package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.plus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.*
import kotlin.time.Duration

/**
 * An update for a [Vision] or a [VisionGroup]
 */
public class VisionChangeBuilder: VisionContainerBuilder<Vision> {
    private val propertyChange = HashMap<Name, Config>()
    private val childrenChange = HashMap<Name, Vision?>()

    public fun isEmpty(): Boolean = propertyChange.isEmpty() && childrenChange.isEmpty()

    public fun propertyChanged(visionName: Name, propertyName: Name, item: MetaItem<*>?) {
        propertyChange.getOrPut(visionName) { Config() }.setItem(propertyName, item)
    }

    override fun set(name: Name, child: Vision?) {
        childrenChange[name] = child
    }

    /**
     * Isolate collected changes by creating detached copies of given visions
     */
    public fun isolate(manager: VisionManager): VisionChange = VisionChange(
        propertyChange,
        childrenChange.mapValues { it.value?.isolate(manager) }
    )
}

private fun Vision.isolate(manager: VisionManager): Vision {
    //TODO replace by efficient deep copy
    val json = manager.encodeToJsonElement(this)
    return manager.decodeFromJson(json)
}

@Serializable
public data class VisionChange(
    val propertyChange: Map<Name, @Serializable(MetaSerializer::class) Meta>,
    val childrenChange: Map<Name, Vision?>) {
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
    mutex: Mutex,
    collector: ()->VisionChangeBuilder,
) {
    //Collect properties change
    source.config.onChange(mutex) { propertyName, oldItem, newItem ->
        if (oldItem != newItem) {
            launch {
                mutex.withLock {
                    collector().propertyChanged(name, propertyName, newItem)
                }
            }
        }
    }

    if (source is VisionGroup) {
        //Subscribe for children changes
        source.children.forEach { (token, child) ->
            collectChange(name + token, child, mutex, collector)
        }
        //TODO update styles?

        //Subscribe for structure change
        if (source is MutableVisionGroup) {
            source.onStructureChange(mutex) { token, before, after ->
                before?.removeChangeListener(mutex)
                (before as? MutableVisionGroup)?.removeStructureChangeListener(mutex)
                if (after != null) {
                    collectChange(name + token, after, mutex, collector)
                }
                collector()[name + token] = after
            }
        }
    }
}

@DFExperimental
public fun Vision.flowChanges(
    manager: VisionManager,
    collectionDuration: Duration,
    scope: CoroutineScope = manager.context,
): Flow<VisionChange> = flow {
    val mutex = Mutex()

    var collector = VisionChangeBuilder()
    scope.collectChange(Name.EMPTY, this@flowChanges, mutex){collector}

    while (true) {
        //Wait for changes to accumulate
        kotlinx.coroutines.delay(collectionDuration)
        //Propagate updates only if something is changed
        if (!collector.isEmpty()) {
            //emit changes
            emit(collector.isolate(manager))
            //Reset the collector
            collector = VisionChangeBuilder()
        }
    }
}