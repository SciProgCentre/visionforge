package space.kscience.visionforge.html

import kotlinx.html.InputType
import kotlinx.html.TagConsumer
import kotlinx.html.stream.createHTML
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.AbstractVision


@Serializable
public abstract class VisionOfHtml : AbstractVision() {
    public var classes: List<String> by properties.stringList(*emptyArray())
}

@Serializable
@SerialName("html.plain")
public class VisionOfPlainHtml : VisionOfHtml() {
    public var content: String? by properties.string()
}

public inline fun VisionOfPlainHtml.content(block: TagConsumer<*>.() -> Unit) {
    content = createHTML().apply(block).finalize()
}

@Serializable
public enum class InputFeedbackMode{
    /**
     * Fire feedback event on `onchange` event
     */
    ONCHANGE,

    /**
     * Fire feedback event on `oninput` event
     */
    ONINPUT,

    /**
     * provide only manual feedback
     */
    NONE
}

@Serializable
@SerialName("html.input")
public open class VisionOfHtmlInput(
    public val inputType: String,
    public val feedbackMode: InputFeedbackMode = InputFeedbackMode.ONCHANGE
) : VisionOfHtml() {
    public var value: Value? by properties.value()
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

