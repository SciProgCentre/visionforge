package space.kscience.visionforge

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.names.Name

/**
 * An event propagated from client to a server
 */
public interface VisionEvent {
    public companion object {
        public val CLICK_EVENT_KEY: Name get() = Name.of("events", "click", "payload")
    }
}

/**
 * An event that consists of custom meta
 */
@Serializable
@SerialName("meta")
public class VisionMetaEvent(public val meta: Meta) : VisionEvent, MetaRepr {
    override fun toMeta(): Meta = meta

    override fun toString(): String = toMeta().toString()

}