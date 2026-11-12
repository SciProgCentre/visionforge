package space.kscience.visionforge

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.*
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

    override val properties: Nothing get() = error("Can't get properties of `NullVision`")

    override val eventFlow: Flow<VisionEvent> = emptyFlow()

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
    public val children: Map<NameToken, VisionChange>? = null,
) : VisionEvent {
    public companion object {
        public val PROPERTY_NODE_REMOVE_VALUE: Value = "@remove".asValue()
    }
}

public fun VisionChange.isEmpty(): Boolean = vision == null && properties == null && children == null


/**
 * An update for a [Vision]
 */
public class VisionChangeCollector : MutableVisionContainer<Vision> {

    private var vision: Vision? = null
    private var properties = MutableMeta()
    private val children: HashMap<NameToken, VisionChangeCollector> = HashMap()

    public operator fun get(name: NameToken): VisionChangeCollector? = children[name]

    public fun isEmpty(): Boolean = properties.isEmpty() && properties.isEmpty() && children.isEmpty()

    @JvmSynchronized
    public fun getOrCreateChange(token: NameToken): VisionChangeCollector =
        children.getOrPut(token) { VisionChangeCollector() }

    @JvmSynchronized
    internal fun reset() {
        vision = null
        properties = MutableMeta()
        children.clear()
    }

    @JvmSynchronized
    public fun propertyChanged(propertyName: Name, item: Meta?) {
        //Write property removal as [Null]
        if (propertyName.isEmpty()) {
            properties = item?.toMutableMeta() ?: MutableMeta()
        } else {
            properties[propertyName] = (item ?: Meta(VisionChange.PROPERTY_NODE_REMOVE_VALUE))
        }
    }

    @JvmSynchronized
    public fun updateProperties(newProperties: Meta) {
        properties.update(newProperties)
    }

    override fun setVision(token: NameToken, vision: Vision?) {
        getOrCreateChange(token).apply {
            this.vision = vision ?: NullVision
        }
    }

    public fun consumeEvent(event: VisionEvent): Unit = when (event) {
        //is VisionChange -> updateFrom(event)
        is VisionEventPack -> event.events.forEach { consumeEvent(it) }

        is VisionEventForChild -> getOrCreateChange(event.childName).consumeEvent(event.event)

        is VisionChange -> {
            event.properties?.let { properties.update(it) }
            event.children?.forEach { (token, child) ->
                getOrCreateChange(token).consumeEvent(child)
            }
            vision = event.vision
        }

//        is SetVisionChildEvent -> setVision(event.nameToken, event.vision)
//
//        is SetVisionPropertiesEvent -> updateProperties(event.properties)
//
//        is SetRootVisionEvent -> vision = event.vision

        //listen to changed event
        is VisionPropertyChangedEvent -> propertyChanged(
            propertyName = event.propertyName,
            item = event.propertyValue
        )

        is VisionGroupCompositionChangedEvent -> setVision(event.childName, event.childVision)

        is VisionControlEvent, is VisionMetaEvent -> {
            //do nothing
            //TODO add logging
        }
    }

    public fun collect(visionManager: VisionManager): VisionChange = VisionChange(
        vision = vision?.deepCopy(visionManager),
        properties = properties.takeIf { !it.isEmpty() },
        children = children.takeIf { !it.isEmpty() }?.mapValues { it.value.collect(visionManager) }
    )
}

public operator fun VisionChangeCollector.get(name: Name): VisionChangeCollector? = when (name.length) {
    0 -> this
    1 -> get(name.first())
    else -> get(name.first())?.get(name.cutFirst())
}

public fun VisionChangeCollector.getOrCreateChange(name: Name): VisionChangeCollector = when (name.length) {
    0 -> this
    1 -> getOrCreateChange(name.first())
    else -> getOrCreateChange(name.first()).getOrCreateChange(name.cutFirst())
}

public inline fun VisionManager.VisionChange(block: VisionChangeCollector.() -> Unit): VisionChange =
    VisionChangeCollector().apply(block).collect(this)

/**
 * Generate a flow of changes for this vision and its children
 *
 * @param sendInitial if true, send the initial vision state as first change
 */
public fun Vision.flowChanges(
    collectionDuration: Duration,
    sendInitial: Boolean = false,
): Flow<VisionEvent> = flow {
    val manager = manager ?: error("Orphan vision could not collect changes")
    coroutineScope {
        val collector = VisionChangeCollector()
        val mutex = Mutex()

        fun collectVisionEvents(vision: Vision, prefix: Name) {
            vision.eventFlow.onEach {
                val event = if (prefix.isEmpty()) it else VisionEventForChild(prefix, it)
                collector.consumeEvent(event)
            }.launchIn(this)
            if (vision is VisionGroup<*>) {
                vision.visions.forEach { (token, child) ->
                    collectVisionEvents(child, prefix + token)
                }
            }
        }

        collectVisionEvents(this@flowChanges, Name.EMPTY)

        if (sendInitial) {
            //Send initial vision state
            val initialEvent = VisionChange(vision = deepCopy(manager))
            emit(initialEvent)
        }

        while (true) {
            //Wait for changes to accumulate
            delay(collectionDuration)
            //Propagate updates only if something is changed
            if (!collector.isEmpty()) {
                mutex.withLock {
                    //emit changes
                    emit(collector.collect(manager))
                    //Reset the collector
                    collector.reset()
                }
            }
        }
    }
}