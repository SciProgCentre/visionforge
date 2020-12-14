package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.plus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.*
import kotlin.time.Duration

/**
 * An update for a [Vision] or a [VisionGroup]
 */
public class VisionChangeBuilder : VisionContainerBuilder<Vision> {

    private val propertyChange = HashMap<Name, Config>()
    private val childrenChange = HashMap<Name, Vision?>()

    public fun isEmpty(): Boolean = propertyChange.isEmpty() && childrenChange.isEmpty()

    public fun propertyChanged(visionName: Name, propertyName: Name, item: MetaItem<*>?) {
        propertyChange
            .getOrPut(visionName) { Config() }
            .setItem(propertyName, item)
    }

    override fun set(name: Name, child: Vision?) {
        childrenChange[name] = child
    }

    /**
     * Isolate collected changes by creating detached copies of given visions
     */
    public fun isolate(manager: VisionManager): VisionChange = VisionChange(
        propertyChange.mapValues { it.value.seal() },
        childrenChange.mapValues { it.value?.isolate(manager) }
    )
    //TODO optimize isolation for visions without parents?
}

private fun Vision.isolate(manager: VisionManager): Vision {
    //TODO replace by efficient deep copy
    val json = manager.encodeToJsonElement(this)
    return manager.decodeFromJson(json)
}

@Serializable
public data class VisionChange(
    val propertyChange: Map<Name, @Serializable(MetaSerializer::class) Meta>,
    val childrenChange: Map<Name, Vision?>,
) {
    public fun isEmpty(): Boolean = propertyChange.isEmpty() && childrenChange.isEmpty()

    /**
     * A shortcut to the top level property dif
     */
    public val properties: Meta? get() = propertyChange[Name.EMPTY]
}

public inline fun VisionChange(manager: VisionManager, block: VisionChangeBuilder.() -> Unit): VisionChange =
    VisionChangeBuilder().apply(block).isolate(manager)


private fun CoroutineScope.collectChange(
    name: Name,
    source: Vision,
    collector: () -> VisionChangeBuilder,
) {

    //Collect properties change
    source.config.onChange(this) { propertyName, oldItem, newItem ->
        if (oldItem != newItem) {
            launch {
                collector().propertyChanged(name, propertyName, newItem)
            }
        }
    }

    coroutineContext[Job]?.invokeOnCompletion {
        source.config.removeListener(this)
    }

    if (source is VisionGroup) {
        //Subscribe for children changes
        source.children.forEach { (token, child) ->
            collectChange(name + token, child, collector)
        }

        //Subscribe for structure change
        if (source is MutableVisionGroup) {
            source.onStructureChange(this) { token, before, after ->
                before?.removeChangeListener(this)
                (before as? MutableVisionGroup)?.removeStructureChangeListener(this)
                if (after != null) {
                    collectChange(name + token, after, collector)
                }
                collector()[name + token] = after
            }
            coroutineContext[Job]?.invokeOnCompletion {
                source.removeStructureChangeListener(this)
            }
        }
    }
}

@DFExperimental
public fun Vision.flowChanges(
    manager: VisionManager,
    collectionDuration: Duration,
): Flow<VisionChange> = flow {

    var collector = VisionChangeBuilder()
    manager.context.collectChange(Name.EMPTY, this@flowChanges) { collector }

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