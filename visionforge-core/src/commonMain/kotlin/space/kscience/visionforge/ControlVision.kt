package space.kscience.visionforge

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.meta.MutableMeta

@Serializable
@SerialName("control")
public abstract class VisionControlEvent : VisionEvent, MetaRepr {
    public abstract val meta: Meta

    override fun toMeta(): Meta = meta
}

public interface ControlVision : Vision {
    public val controlEventFlow: Flow<VisionControlEvent>

    public fun dispatchControlEvent(event: VisionControlEvent)

    override fun receiveEvent(event: VisionEvent) {
        if (event is VisionControlEvent) {
            dispatchControlEvent(event)
        } else super.receiveEvent(event)
    }
}

@Serializable
@SerialName("control.click")
public class VisionClickEvent(override val meta: Meta) : VisionControlEvent()


public interface ClickControl : ControlVision {
    public fun click(builder: MutableMeta.() -> Unit = {}) {
        dispatchControlEvent(VisionClickEvent(Meta(builder)))
    }

    public fun onClick(scope: CoroutineScope, block: suspend VisionClickEvent.() -> Unit): Job {
        return controlEventFlow.filterIsInstance<VisionClickEvent>().onEach(block).launchIn(scope)
    }

    public companion object {

    }
}