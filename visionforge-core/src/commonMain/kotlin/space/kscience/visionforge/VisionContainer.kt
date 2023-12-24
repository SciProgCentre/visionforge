package space.kscience.visionforge

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.kscience.dataforge.names.*
import space.kscience.visionforge.VisionChildren.Companion.STATIC_TOKEN_BODY

@DslMarker
public annotation class VisionBuilder

/**
 * A container interface with read access to its content
 * using DataForge [Name] objects as keys.
 */
public interface VisionContainer<out V : Vision> {
    public fun getChild(name: Name): V?
}

/**
 * A container interface with write/replace/delete access to its content.
 */
public interface MutableVisionContainer<in V : Vision> {
    //TODO add documentation
    public fun setChild(name: Name?, child: V?)
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

    override fun getChild(name: Name): Vision? = when (name.length) {
        0 -> parent
        1 -> get(name.first())
        else -> get(name.first())?.children?.getChild(name.cutFirst())
    }

    public companion object {
        public const val STATIC_TOKEN_BODY: String = "@static"

        public fun empty(owner: Vision): VisionChildren = object : VisionChildren {
            override val parent: Vision get() = owner
            override val keys: Set<NameToken> get() = emptySet()
            override val changes: Flow<Name> get() = emptyFlow()
            override fun get(token: NameToken): Vision? = null
        }
    }
}

public operator fun VisionChildren.get(name: Name): Vision? = getChild(name)
public operator fun VisionChildren.get(name: String): Vision? = getChild(name)


public fun VisionChildren.isEmpty(): Boolean = keys.isEmpty()

public inline fun VisionChildren.forEach(block: (NameToken, Vision) -> Unit) {
    keys.forEach { block(it, get(it)!!) }
}

/**
 * A serializable representation of [Vision] children container
 * with the ability to modify the container content.
 */
public interface MutableVisionChildren : VisionChildren, MutableVisionContainer<Vision> {

    public override val parent: MutableVisionGroup

    public operator fun set(token: NameToken, value: Vision?)

    /**
     * Set child [Vision] by name.
     * @param name child name. Pass null to add a static child. Note that static children cannot
     *  be removed, replaced or accessed by name by other means.
     * @param child new child value. Pass null to delete the child.
     */
    override fun setChild(name: Name?, child: Vision?) {
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
                val parent: MutableVisionGroup = currentParent as? MutableVisionGroup ?: parent.createGroup().also {
                    set(name.first(), it)
                }
                parent.children.setChild(name.cutFirst(), child)
            }
        }
    }

    public fun clear()
}

public operator fun MutableVisionChildren.set(name: Name?, vision: Vision?) {
    setChild(name, vision)
}

public operator fun MutableVisionChildren.set(name: String?, vision: Vision?) {
    setChild(name, vision)
}


/**
 * Add a static child. Statics could not be found by name, removed or replaced. Changing statics also do not trigger events.
 */
public fun MutableVisionChildren.static(child: Vision) {
    set(NameToken(STATIC_TOKEN_BODY, index = child.hashCode().toString()), child)
}

public fun VisionChildren.asSequence(): Sequence<Pair<NameToken, Vision>> = sequence {
    keys.forEach { yield(it to get(it)!!) }
}

public operator fun VisionChildren.iterator(): Iterator<Pair<NameToken, Vision>> = asSequence().iterator()

public fun <V : Vision> VisionContainer<V>.getChild(str: String): V? = getChild(Name.parse(str))

public fun <V : Vision> MutableVisionContainer<V>.setChild(
    str: String?, vision: V?,
): Unit = setChild(str?.parseAsName(), vision)

internal abstract class VisionChildrenImpl(
    override val parent: MutableVisionGroup,
) : MutableVisionChildren {

    private val updateJobs = HashMap<NameToken, Job>()

    abstract var items: MutableMap<NameToken, Vision>?

    @JvmSynchronized
    private fun buildItems(): MutableMap<NameToken, Vision> {
        if (items == null) {
            items = LinkedHashMap()
        }
        return items!!
    }

    private val scope: CoroutineScope? get() = parent.manager?.context

    override val keys: Set<NameToken> get() = items?.keys ?: emptySet()

    override fun get(token: NameToken): Vision? = items?.get(token)

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
            items?.remove(token)
        } else {
            (items ?: buildItems())[token] = value
            //check if parent already exists and is different from the current one
            if (value.parent != null && value.parent != parent) error("Can't reassign parent Vision for $value")
            //set parent
            value.parent = parent
            //start update jobs (only if the vision is rooted)
            scope?.let { scope ->
                val job = value.children?.changes?.onEach {
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
        items?.clear()
        updateJobs.values.forEach { it.cancel() }
        updateJobs.clear()
        onChange(Name.EMPTY)
    }
}

