package space.kscience.visionforge

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.Value

@Serializable
@SerialName("control")
public abstract class VisionControlEvent : VisionEvent, MetaRepr {
    public abstract val meta: Meta

    override fun toMeta(): Meta = meta
}

public interface ControlVision : Vision {
    public val controlEventFlow: SharedFlow<VisionControlEvent>

    public fun dispatchControlEvent(event: VisionControlEvent)

    override fun receiveEvent(event: VisionEvent) {
        if (event is VisionControlEvent) {
            dispatchControlEvent(event)
        } else super.receiveEvent(event)
    }
}

/**
 * @param payload The optional payload associated with the click event.
 */
@Serializable
@SerialName("control.click")
public class VisionClickEvent(public val payload: Meta = Meta.EMPTY) : VisionControlEvent() {
    override val meta: Meta get() = Meta { ::payload.name put payload }
}


public interface ClickControl : ControlVision {
    /**
     * Create and dispatch a click event
     */
    public fun click(builder: MutableMeta.() -> Unit = {}) {
        dispatchControlEvent(VisionClickEvent(Meta(builder)))
    }
}

/**
 * Register listener
 */
public fun ClickControl.onClick(scope: CoroutineScope, block: suspend VisionClickEvent.() -> Unit): Job =
    controlEventFlow.filterIsInstance<VisionClickEvent>().onEach(block).launchIn(scope)


@Serializable
@SerialName("control.valueChange")
public class VisionValueChangeEvent(override val meta: Meta) : VisionControlEvent() {

    public val value: Value? get() = meta.value
}

public fun VisionValueChangeEvent(value: Value?): VisionValueChangeEvent =
    VisionValueChangeEvent(Meta { this.value = value })
