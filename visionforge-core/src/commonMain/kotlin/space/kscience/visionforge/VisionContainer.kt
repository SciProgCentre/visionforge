package space.kscience.visionforge

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import space.kscience.dataforge.names.*

@DslMarker
public annotation class VisionBuilder

public interface VisionContainer<out V : Vision> {
    public operator fun get(name: Name): V?
}

public interface VisionContainerBuilder<in V : Vision> {
    //TODO add documentation
    public operator fun set(name: Name?, child: V?)
}

/**
 * A serializable representation of [Vision] children container
 */
public interface VisionChildren : VisionContainer<Vision> {
    public val parent: Vision?

    public val keys: Set<NameToken>

    public val values: Iterable<Vision> get() = keys.map { get(it)!! }

    public val changes: Flow<Name>

    public operator fun get(token: NameToken): Vision?

    override fun get(name: Name): Vision? = when (name.length) {
        0 -> parent
        1 -> get(name.first())
        else -> get(name.first())?.children?.get(name.cutFirst())
    }

    public companion object {
        public fun empty(owner: Vision): VisionChildren = object : VisionChildren {
            override val parent: Vision get() = owner
            override val keys: Set<NameToken> get() = emptySet()
            override val changes: Flow<Name> get() = emptyFlow()
            override fun get(token: NameToken): Vision? = null
        }
    }
}

public fun VisionChildren.isEmpty(): Boolean = keys.isEmpty()

@Serializable(VisionChildrenContainerSerializer::class)
public interface MutableVisionChildren : VisionChildren, VisionContainerBuilder<Vision> {
    public override val parent: MutableVisionGroup?

    public operator fun set(token: NameToken, value: Vision?)

    override fun set(name: Name?, child: Vision?) {
        when {
            name == null -> {
                if (child != null) {
                    static(child)
                }
            }

            name.isEmpty() -> error("Empty names are not allowed in VisionGroup::set")
            name.length == 1 -> {
                val token = name.tokens.first()
                set(token, child)
            }

            else -> {
                val currentParent = get(name.first())
                if (currentParent != null && currentParent !is MutableVisionGroup) error("Can't assign a child to $currentParent")
                val parent: MutableVisionGroup = currentParent as? MutableVisionGroup ?: parent?.createGroup().also {
                    set(name.first(), it)
                } ?: error("Container owner not set")
                parent.children[name.cutFirst()] = child
            }
        }
    }

    public fun clear()
}

/**
 * Add a static child. Statics could not be found by name, removed or replaced. Changing statics also do not trigger events.
 */
public fun MutableVisionChildren.static(child: Vision): Unit {
    set(NameToken("@static", index = child.hashCode().toString()), child)
}

public fun VisionChildren.asSequence(): Sequence<Pair<NameToken, Vision>> = sequence {
    keys.forEach { yield(it to get(it)!!) }
}

public operator fun VisionChildren.iterator(): Iterator<Pair<NameToken, Vision>> = asSequence().iterator()

public operator fun <V : Vision> VisionContainer<V>.get(str: String): V? = get(Name.parse(str))

public operator fun <V : Vision> VisionContainerBuilder<V>.set(
    str: String?, vision: V?,
): Unit = set(str?.parseAsName(), vision)

internal class VisionChildrenImpl(
    items: Map<NameToken, Vision>,
) : MutableVisionChildren {

    override var parent: MutableVisionGroup? = null
        internal set

    private val items = LinkedHashMap(items)
    private val updateJobs = HashMap<NameToken, Job>()

    private val scope: CoroutineScope? get() = parent?.manager?.context

    override val keys: Set<NameToken> get() = items.keys

    override fun get(token: NameToken): Vision? = items[token]

    private val _changes = MutableSharedFlow<Name>()
    override val changes: SharedFlow<Name> get() = _changes

    private fun onChange(name: Name) {
        scope?.launch {
            _changes.emit(name)
        }
    }

    override operator fun set(token: NameToken, value: Vision?) {
        //fast return if value equals existing
        if (value == get(token)) return

        val currentUpdateJob = updateJobs[token]
        if (currentUpdateJob != null) {
            currentUpdateJob.cancel()
            updateJobs.remove(token)
        }

        if (value == null) {
            items.remove(token)
        } else {
            items[token] = value
            //check if parent already exists and is different from the current one
            if (value.parent != null && value.parent != parent) error("Can't reassign parent Vision for $value")
            //set parent
            value.parent = parent
            //start update jobs (only if the vision is rooted)
            scope?.let { scope ->
                val job = (value.children as? VisionChildrenImpl)?.changes?.onEach {
                    onChange(token + it)
                }?.launchIn(scope)
                if (job != null) {
                    updateJobs[token] = job
                }
            }
        }

        onChange(token.asName())
    }

    override fun clear() {
        if (items.isNotEmpty()) {
            updateJobs.values.forEach {
                it.cancel()
            }
            updateJobs.clear()
            items.clear()
            onChange(Name.EMPTY)
        }
    }
}

internal object VisionChildrenContainerSerializer : KSerializer<MutableVisionChildren> {
    private val mapSerializer = serializer<Map<NameToken,Vision>>()

    override val descriptor: SerialDescriptor = mapSerializer.descriptor

    override fun deserialize(decoder: Decoder): MutableVisionChildren {
        val map = decoder.decodeSerializableValue(mapSerializer)
        return VisionChildrenImpl(map)
    }

    override fun serialize(encoder: Encoder, value: MutableVisionChildren) {
        val map = value.keys.associateWith { value[it]!! }
        encoder.encodeSerializableValue(mapSerializer, map)
    }

}
