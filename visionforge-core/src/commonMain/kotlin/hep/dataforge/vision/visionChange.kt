package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.Name
import hep.dataforge.names.isEmpty
import hep.dataforge.names.plus
import hep.dataforge.vision.VisionManager.Companion.visionSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration


public abstract class EmptyVision : Vision {

    @Suppress("SetterBackingFieldAssignment", "UNUSED_PARAMETER")
    override var parent: VisionGroup? = null
        set(value) {
            //do nothing
        }

    override val properties: Config? = null

    override val allProperties: Laminate
        get() = Laminate(Meta.EMPTY)

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? = null

    override fun propertyChanged(name: Name) {}

    override fun onPropertyChange(owner: Any?, action: (Name) -> Unit) {}

    override fun removeChangeListener(owner: Any?) {}

    override fun update(change: Vision) {
        error("Null vision should be removed, not updated")
    }

    override val config: Config get() = Config()
    override val descriptor: NodeDescriptor? get() = null
}

/**
 * An empty vision existing only for Vision tree change representation. [NullVision] should not be used outside update logic.
 */
@Serializable
@SerialName("vision.null")
public object NullVision : EmptyVision()

/**
 * Serialization proxy is used to create immutable reference for a given vision
 */
@Serializable(VisionSerializationProxy.Companion::class)
private class VisionSerializationProxy(val ref: Vision) : EmptyVision() {
    companion object : KSerializer<VisionSerializationProxy> {
        override val descriptor: SerialDescriptor = visionSerializer.descriptor

        @OptIn(ExperimentalSerializationApi::class)
        override fun serialize(encoder: Encoder, value: VisionSerializationProxy) {
            val serializer = encoder.serializersModule.getPolymorphic(Vision::class, value.ref)
                ?: error("The polymorphic serializer is not provided for ")
            serializer.serialize(encoder, value.ref)
        }

        override fun deserialize(decoder: Decoder): VisionSerializationProxy =
            VisionSerializationProxy(visionSerializer.deserialize(decoder))
    }
}


private fun MutableVisionGroup.getOrCreate(name: Name): Vision {
    if (name.isEmpty()) return this
    val existing = get(name)
    return existing ?: VisionGroupBase().also { set(name, it) }
}

private fun CoroutineScope.collectChange(
    name: Name,
    source: Vision,
    mutex: Mutex,
    target: () -> MutableVisionGroup,
) {
    //Collect properties change
    source.config.onChange(mutex){propertyName, oldItem, newItem->
        if(oldItem!= newItem){
            launch {
                mutex.withLock {
                    target().getOrCreate(name).setProperty(propertyName, newItem)
                }
            }
        }
    }
//    source.onPropertyChange(mutex) { propertyName ->
//        launch {
//            mutex.withLock {
//                target().getOrCreate(name).setProperty(propertyName, source.getProperty(propertyName,false))
//            }
//        }
//    }

    val targetVision: Vision =  target().getOrCreate(name)

    if (source is VisionGroup) {
        check(targetVision is MutableVisionGroup) { "Collector for a group should be a group" }
        //Subscribe for children changes
        source.children.forEach { (token, child) ->
            collectChange(name + token, child, mutex, target)
        }
        //TODO update styles?

        //Subscribe for structure change
        if (source is MutableVisionGroup) {
            source.onStructureChange(mutex) { token, before, after ->
                before?.removeChangeListener(mutex)
                (before as? MutableVisionGroup)?.removeStructureChangeListener(mutex)
                if (after != null) {
                    targetVision[token] = VisionSerializationProxy(after)
                    collectChange(name + token, after, mutex, target)
                } else {
                    targetVision[token] = NullVision
                }
            }
        }
    }
}

@DFExperimental
public fun Vision.flowChanges(scope: CoroutineScope, collectionDuration: Duration): Flow<Vision> = flow {
    val mutex = Mutex()

    var collector = VisionGroupBase()
    scope.collectChange(Name.EMPTY, this@flowChanges, mutex) { collector }

    while (true) {
        //Wait for changes to accumulate
        kotlinx.coroutines.delay(collectionDuration)
        //Propagate updates only if something is changed
        if (collector.children.isNotEmpty() || collector.properties?.isEmpty() != false) {
            //emit changes
            emit(collector)
            //Reset the collector
            collector = VisionGroupBase()
        }
    }
}