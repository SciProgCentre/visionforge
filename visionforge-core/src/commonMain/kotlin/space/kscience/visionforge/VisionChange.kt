package space.kscience.visionforge

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.isEmpty
import space.kscience.dataforge.names.plus
import kotlin.time.Duration



/**
 * A vision used only in change propagation and showing that the target should be removed
 */
@Serializable
@SerialName("null")
public object NullVision : Vision {
    override var parent: Vision?
        get() = null
        set(_) {
            error("Can't set parent for null vision")
        }

    override val properties: MutableVisionProperties get() = error("Can't get properties of `NullVision`")

    override val descriptor: MetaDescriptor? = null
}

/**
 * Create a deep copy of given Vision without external connections.
 */
private fun Vision.deepCopy(manager: VisionManager): Vision {
    if (this is NullVision) return NullVision

    //Assuming that unrooted visions are already isolated
    //TODO replace by efficient deep copy
    val json = manager.encodeToJsonElement(this)
    return manager.decodeFromJson(json)
}


/**
 * An event that contains changes made to a vision.
 *
 * @param vision a new value for vision content. If the Vision is to be removed should be [NullVision]
 * @param properties updated properties
 * @param children a map of children changed in ths [VisionChange].
 */
@Serializable
@SerialName("change")
public data class VisionChange(
    public val vision: Vision? = null,
    public val properties: Meta? = null,
    public val children: Map<Name, VisionChange>? = null,
) : VisionEvent

/**
 * An update for a [Vision]
 */
public class VisionChangeBuilder : MutableVisionContainer<Vision> {

    private var vision: Vision? = null
    private var propertyChange = MutableMeta()
    private val children: HashMap<Name, VisionChangeBuilder> = HashMap()

    public operator fun get(name: Name): VisionChangeBuilder? = children[name]

    public fun isEmpty(): Boolean = propertyChange.isEmpty() && propertyChange.isEmpty() && children.isEmpty()

    @JvmSynchronized
    private fun getOrPutChild(visionName: Name): VisionChangeBuilder =
        children.getOrPut(visionName) { VisionChangeBuilder() }

    @JvmSynchronized
    internal fun reset() {
        vision = null
        propertyChange = MutableMeta()
        children.clear()
    }

    public fun propertyChanged(visionName: Name, propertyName: Name, item: Meta?) {
        if (visionName == Name.EMPTY) {
            //Write property removal as [Null]
            if (propertyName.isEmpty()) {
                propertyChange = item?.toMutableMeta() ?: MutableMeta()
            } else {
                propertyChange[propertyName] = (item ?: Meta(Null))
            }
        } else {
            getOrPutChild(visionName).propertyChanged(Name.EMPTY, propertyName, item)
        }
    }

    override fun setChild(name: Name?, child: Vision?) {
        if (name == null) error("Static children are not allowed in VisionChange")
        getOrPutChild(name).apply {
            vision = child ?: NullVision
        }
    }

    private fun build(visionManager: VisionManager): VisionChange = VisionChange(
        vision,
        if (propertyChange.isEmpty()) null else propertyChange,
        if (children.isEmpty()) null else children.mapValues { it.value.build(visionManager) }
    )

    /**
     * Isolate collected changes by creating detached copies of given visions
     */
    public fun deepCopy(visionManager: VisionManager): VisionChange = VisionChange(
        vision?.deepCopy(visionManager),
        if (propertyChange.isEmpty()) null else propertyChange.seal(),
        if (children.isEmpty()) null else children.mapValues { it.value.deepCopy(visionManager) }
    )

    /**
     * Transform current change directly to Json string without protective copy
     */
    public fun toJsonString(visionManager: VisionManager): String = visionManager.encodeToString(
        build(visionManager)
    )
}

public inline fun VisionManager.VisionChange(block: VisionChangeBuilder.() -> Unit): VisionChange =
    VisionChangeBuilder().apply(block).deepCopy(this)


/**
 * Collect changes that are made to [source] to [collector] using [mutex] as a synchronization lock.
 */
private fun CoroutineScope.collectChange(
    name: Name,
    source: Vision,
    mutex: Mutex,
    collector: VisionChangeBuilder,
) {

    //Collect properties change
    source.properties.flowChanges().onEach { propertyName ->
        val newItem = source.properties.own[propertyName]
        collector.propertyChanged(name, propertyName, newItem)
    }.launchIn(this)

    val children = source.children
    //Subscribe for children changes
    children?.forEach { token, child ->
        collectChange(name + token, child, mutex, collector)
    }

    //Subscribe for structure change
    children?.changes?.onEach { changedName ->
        val after = children[changedName]
        val fullName = name + changedName
        if (after != null) {
            collectChange(fullName, after, mutex, collector)
        }
        mutex.withLock {
            collector.setChild(fullName, after)
        }
    }?.launchIn(this)
}

/**
 * Generate a flow of changes of this vision and its children
 *
 * @param sendInitial if true, send the initial vision state as first change
 */
public fun Vision.flowChanges(
    collectionDuration: Duration,
    sendInitial: Boolean = false,
): Flow<VisionChange> = flow {
    val manager = manager ?: error("Orphan vision could not collect changes")
    coroutineScope {
        val collector = VisionChangeBuilder()
        val mutex = Mutex()
        collectChange(Name.EMPTY, this@flowChanges, mutex, collector)

        if (sendInitial) {
            //Send initial vision state
            val initialChange = VisionChange(vision = deepCopy(manager))
            emit(initialChange)
        }

        while (true) {
            //Wait for changes to accumulate
            delay(collectionDuration)
            //Propagate updates only if something is changed
            if (!collector.isEmpty()) {
                mutex.withLock {
                    //emit changes
                    emit(collector.deepCopy(manager))
                    //Reset the collector
                    collector.reset()
                }
            }
        }
    }
}