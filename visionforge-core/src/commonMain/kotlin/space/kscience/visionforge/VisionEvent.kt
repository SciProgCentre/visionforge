package space.kscience.visionforge

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.Name

/**
 * An event propagated from client to a server
 */
@Serializable
public sealed interface VisionEvent

/**
 * An event that consists of custom meta
 */
@Serializable
@SerialName("meta")
public class VisionMetaEvent(public val targetName: Name, public val meta: Meta) : VisionEvent


/**
 * A vision used only in change propagation and showing that the target should be removed
 */
@Serializable
@SerialName("null")
public object NullVision : Vision {
    override var parent: Vision?
        get() = null
        set(_) {
            error("Can't set parent for null vision")
        }

    override val properties: MutableVisionProperties get() = error("Can't get properties of `NullVision`")

    override val descriptor: MetaDescriptor? = null
}


/**
 * @param vision a new value for vision content. If the Vision is to be removed should be [NullVision]
 * @param properties updated properties
 * @param children a map of children changed in ths [VisionChange].
 */
@Serializable
@SerialName("change")
public data class VisionChange(
    public val vision: Vision? = null,
    public val properties: Meta? = null,
    public val children: Map<Name, VisionChange>? = null,
) : VisionEvent