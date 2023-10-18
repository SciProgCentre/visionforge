package space.kscience.visionforge

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * An event propagated from client to a server
 */
@Serializable
public sealed interface VisionEvent{
    public val targetName: Name
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