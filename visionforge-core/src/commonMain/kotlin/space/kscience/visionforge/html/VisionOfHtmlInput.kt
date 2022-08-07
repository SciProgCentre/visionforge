package space.kscience.visionforge.html

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.boolean
import space.kscience.dataforge.meta.number
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.*

//TODO replace by something
internal val Vision.mutableProperties get() = properties[Name.EMPTY, false, false]

@Serializable
public abstract class VisionOfHtmlInput : AbstractVision() {
    public var disabled: Boolean by mutableProperties.boolean { false }
}

@Serializable
@SerialName("html.text")
public class VisionOfTextField(
    public val label: String? = null,
    public val name: String? = null,
) : VisionOfHtmlInput() {
    public var text: String? by mutableProperties.string()
}

@Serializable
@SerialName("html.checkbox")
public class VisionOfCheckbox(
    public val label: String? = null,
    public val name: String? = null,
) : VisionOfHtmlInput() {
    public var checked: Boolean? by mutableProperties.boolean()
}

@Serializable
@SerialName("html.number")
public class VisionOfNumberField(
    public val label: String? = null,
    public val name: String? = null,
) : VisionOfHtmlInput() {
    public var value: Number? by mutableProperties.number()
}

@Serializable
@SerialName("html.range")
public class VisionOfRangeField(
    public val min: Double,
    public val max: Double,
    public val step: Double = 1.0,
    public val label: String? = null,
    public val name: String? = null,
) : VisionOfHtmlInput() {
    public var value: Number? by mutableProperties.number()
}

