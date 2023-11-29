package space.kscience.visionforge.html

import kotlinx.html.InputType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.AbstractVision


@Serializable
public abstract class VisionOfHtml: AbstractVision(){
    public var classes: List<String> by properties.stringList(*emptyArray())
}

@Serializable
@SerialName("html.input")
public open class VisionOfHtmlInput(
    public val inputType: String,
) : VisionOfHtml() {
    public var value : Value? by properties.value()
    public var disabled: Boolean by properties.boolean { false }
    public var fieldName: String? by properties.string()
}


@Serializable
@SerialName("html.text")
public class VisionOfTextField : VisionOfHtmlInput(InputType.text.realValue) {
    public var text: String? by properties.string(key = VisionOfHtmlInput::value.name.asName())
}

@Serializable
@SerialName("html.checkbox")
public class VisionOfCheckbox : VisionOfHtmlInput(InputType.checkBox.realValue) {
    public var checked: Boolean? by properties.boolean(key = VisionOfHtmlInput::value.name.asName())
}

@Serializable
@SerialName("html.number")
public class VisionOfNumberField : VisionOfHtmlInput(InputType.number.realValue) {
    public var number: Number? by properties.number(key = VisionOfHtmlInput::value.name.asName())
}

@Serializable
@SerialName("html.range")
public class VisionOfRangeField(
    public val min: Double,
    public val max: Double,
    public val step: Double = 1.0,
) : VisionOfHtmlInput(InputType.range.realValue) {
    public var number: Number? by properties.number(key = VisionOfHtmlInput::value.name.asName())
}

