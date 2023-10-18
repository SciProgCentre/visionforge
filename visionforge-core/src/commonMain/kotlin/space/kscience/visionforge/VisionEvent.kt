package space.kscience.visionforge

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.set
import space.kscience.dataforge.names.Name

/**
 * An event propagated from client to a server
 */
@Serializable
public sealed interface VisionEvent{
    public val targetName: Name

    public companion object{
        public val CLICK_EVENT_KEY: Name get() = Name.of("events", "click", "payload")
    }
}

/**
 * An event that consists of custom meta
 */
@Serializable
@SerialName("meta")
public class VisionMetaEvent(override val targetName: Name, public val meta: Meta) : VisionEvent

@Serializable
@SerialName("change")
public class VisionChangeEvent(override val targetName: Name, public val change: VisionChange) : VisionEvent

public val Vision.Companion.CLICK_EVENT_KEY: Name get() = Name.of("events", "click", "payload")

/**
 * Set the payload to be sent to server on click
 */
public fun Vision.onClickPayload(payloadBuilder: MutableMeta.() -> Unit){
    properties[VisionEvent.CLICK_EVENT_KEY] = Meta(payloadBuilder)
}