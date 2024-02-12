package space.kscience.visionforge

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.parseAsName

public abstract class VisionControlEvent : VisionEvent, MetaRepr {
    public abstract val meta: Meta

    override fun toMeta(): Meta = meta

    override fun toString(): String = toMeta().toString()
}

public interface ControlVision : Vision {
    public val controlEventFlow: SharedFlow<VisionControlEvent>

    public suspend fun dispatchControlEvent(event: VisionControlEvent)

    override suspend fun receiveEvent(event: VisionEvent) {
        if (event is VisionControlEvent) {
            dispatchControlEvent(event)
        } else super.receiveEvent(event)
    }
}


/**
 * An event for submitting changes
 */
@Serializable
@SerialName("control.submit")
public class VisionSubmitEvent(override val meta: Meta) : VisionControlEvent() {
    public val payload: Meta get() = meta[::payload.name] ?: Meta.EMPTY

    public val name: Name? get() = meta["name"].string?.parseAsName()

    override fun toString(): String = meta.toString()
}

public fun VisionSubmitEvent(payload: Meta = Meta.EMPTY, name: Name? = null): VisionSubmitEvent = VisionSubmitEvent(
    Meta {
        VisionSubmitEvent::payload.name put payload
        VisionSubmitEvent::name.name put name.toString()
    }
)


public interface DataControl : ControlVision {
    /**
     * Create and dispatch submit event
     */
    public suspend fun submit(builder: MutableMeta.() -> Unit = {}) {
        dispatchControlEvent(VisionSubmitEvent(Meta(builder)))
    }
}

/**
 * Register listener
 */
public fun DataControl.onSubmit(scope: CoroutineScope, block: suspend VisionSubmitEvent.() -> Unit): Job =
    controlEventFlow.filterIsInstance<VisionSubmitEvent>().onEach(block).launchIn(scope)


@Serializable
@SerialName("control.valueChange")
public class VisionValueChangeEvent(override val meta: Meta) : VisionControlEvent() {

    public val value: Value? get() = meta.value

    /**
     * The name of a control that fired the event
     */
    public val name: Name? get() = meta["name"]?.string?.parseAsName()

    override fun toString(): String = meta.toString()
}


public fun VisionValueChangeEvent(value: Value?, name: Name? = null): VisionValueChangeEvent = VisionValueChangeEvent(
    Meta {
        this.value = value
        name?.let { set("name", it.toString()) }
    }
)


@Serializable
@SerialName("control.input")
public class VisionInputEvent(override val meta: Meta) : VisionControlEvent() {

    public val value: Value? get() = meta.value

    /**
     * The name of a control that fired the event
     */
    public val name: Name? get() = meta["name"]?.string?.parseAsName()

    override fun toString(): String = meta.toString()
}

public fun VisionInputEvent(value: Value?, name: Name? = null): VisionInputEvent = VisionInputEvent(
    Meta {
        this.value = value
        name?.let { set("name", it.toString()) }
    }
)
