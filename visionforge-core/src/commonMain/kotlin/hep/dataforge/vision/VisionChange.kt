package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.plus
import hep.dataforge.values.Null
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.*
import kotlin.jvm.Synchronized
import kotlin.time.Duration

/**
 * An update for a [Vision] or a [VisionGroup]
 */
public class VisionChangeBuilder : VisionContainerBuilder<Vision> {

    private var reset: Boolean = false
    private var vision: Vision? = null
    private val propertyChange = Config()
    private val children: HashMap<Name, VisionChangeBuilder> = HashMap()

    public fun isEmpty(): Boolean = propertyChange.isEmpty() && propertyChange.isEmpty() && children.isEmpty()

    @Synchronized
    private fun getOrPutChild(visionName: Name): VisionChangeBuilder =
        children.getOrPut(visionName) { VisionChangeBuilder() }

    public fun propertyChanged(visionName: Name, propertyName: Name, item: MetaItem?) {
        if (visionName == Name.EMPTY) {
            //Write property removal as [Null]
            propertyChange[propertyName] = (item ?: Null.asMetaItem())
        } else {
            getOrPutChild(visionName).propertyChanged(Name.EMPTY, propertyName, item)
        }
    }

    override fun set(name: Name, child: Vision?) {
        getOrPutChild(name).apply {
            vision = child
            reset = vision == null
        }
    }

    /**
     * Isolate collected changes by creating detached copies of given visions
     */
    public fun isolate(manager: VisionManager): VisionChange = VisionChange(
        reset,
        vision?.isolate(manager),
        if (propertyChange.isEmpty()) null else propertyChange.seal(),
        if (children.isEmpty()) null else children.mapValues { it.value.isolate(manager) }
    )
}

private fun Vision.isolate(manager: VisionManager): Vision {
    //TODO replace by efficient deep copy
    val json = manager.encodeToJsonElement(this)
    return manager.decodeFromJson(json)
}

@Serializable
public data class VisionChange(
    public val reset: Boolean = false,
    public val vision: Vision? = null,
    @Serializable(MetaSerializer::class) public val properties: Meta? = null,
    public val children: Map<Name, VisionChange>? = null,
)

public inline fun VisionChange(manager: VisionManager, block: VisionChangeBuilder.() -> Unit): VisionChange =
    VisionChangeBuilder().apply(block).isolate(manager)


private fun CoroutineScope.collectChange(
    name: Name,
    source: Vision,
    collector: () -> VisionChangeBuilder,
) {

    //Collect properties change
    source.onPropertyChange(this) { propertyName ->
        val newItem = source.getOwnProperty(propertyName)
        collector().propertyChanged(name, propertyName, newItem)
    }

    if (source is VisionGroup) {
        //Subscribe for children changes
        source.children.forEach { (token, child) ->
            collectChange(name + token, child, collector)
        }

        //Subscribe for structure change
        if (source is MutableVisionGroup) {
            source.structureChanges.onEach { (token, _, after) ->
                if (after != null) {
                    collectChange(name + token, after, collector)
                }
                collector()[name + token] = after
            }.launchIn(this)
        }
    }
}

@DFExperimental
public fun Vision.flowChanges(
    manager: VisionManager,
    collectionDuration: Duration,
): Flow<VisionChange> = flow {

    var collector = VisionChangeBuilder()
    coroutineScope {
        collectChange(Name.EMPTY, this@flowChanges) { collector }

        //Send initial vision state
        val initialChange = VisionChange(vision = isolate(manager))
        emit(initialChange)

        while (currentCoroutineContext().isActive) {
            //Wait for changes to accumulate
            delay(collectionDuration)
            //Propagate updates only if something is changed
            if (!collector.isEmpty()) {
                //emit changes
                emit(collector.isolate(manager))
                //Reset the collector
                collector = VisionChangeBuilder()
            }
        }
    }
}