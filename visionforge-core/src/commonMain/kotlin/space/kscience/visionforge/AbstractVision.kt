package space.kscience.visionforge

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.dataforge.meta.ObservableMutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor


@Serializable
public abstract class AbstractVision(
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(ObservableMutableMetaSerializer::class)
    final override val properties: ObservableMutableMeta = ObservableMutableMeta(),
) : MutableVision, AutoCloseable {

    @Transient
    private val _eventFlow =
        MutableSharedFlow<VisionEvent>(extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override val eventFlow: SharedFlow<VisionEvent> get() = _eventFlow

    protected fun emitEvent(event: VisionEvent) {
        val context = manager?.context
        if (context == null) {
            _eventFlow.tryEmit(event)
        } else {
            context.launch {
                _eventFlow.emit(event)
            }
        }
    }

    init {
        properties.onChange(_eventFlow) { name ->
            emitEvent(VisionPropertyChangedEvent(name, properties[name]))
        }
    }

    @Transient
    override var parent: Vision? = null
        set(value) {
            if (value == this) {
                error("Circular parent")
            } else if (value != null && field != null && field != value) {
                error("Parent is already set")
            } else {
                field = value
            }
        }

    override val descriptor: MetaDescriptor? get() = null

    override fun close() {
        properties.removeListener(_eventFlow)
    }

}