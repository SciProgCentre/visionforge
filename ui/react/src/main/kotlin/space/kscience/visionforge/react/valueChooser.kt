package space.kscience.visionforge.react

import kotlinx.css.margin
import kotlinx.css.pct
import kotlinx.css.px
import kotlinx.css.width
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onKeyDownFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import react.*
import react.dom.attrs
import react.dom.option
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.allowedValues
import space.kscience.dataforge.values.ValueType
import space.kscience.dataforge.values.asValue
import space.kscience.dataforge.values.int
import space.kscience.dataforge.values.string
import space.kscience.visionforge.Colors
import space.kscience.visionforge.widgetType
import styled.css
import styled.styledInput
import styled.styledSelect

public external interface ValueChooserProps : RProps {
    public var descriptor: MetaDescriptor?
    public var meta: ObservableMutableMeta
    public var actual: Meta
}

@JsExport
public val StringValueChooser: FunctionComponent<ValueChooserProps> =
    functionalComponent("StringValueChooser") { props ->
        var value by useState(props.actual.string ?: "")
        val keyDown: (Event) -> Unit = { event ->
            if (event.type == "keydown" && event.asDynamic().key == "Enter") {
                value = (event.target as HTMLInputElement).value
                props.meta.value = value.asValue()
            }
        }
        val handleChange: (Event) -> Unit = {
            value = (it.target as HTMLInputElement).value
        }
        styledInput(type = InputType.text) {
            css {
                width = 100.pct
            }
            attrs {
                this.value = value
                onKeyDownFunction = keyDown
                onChangeFunction = handleChange
            }
        }
    }

@JsExport
public val BooleanValueChooser: FunctionComponent<ValueChooserProps> =
    functionalComponent("BooleanValueChooser") { props ->
        val handleChange: (Event) -> Unit = {
            val newValue = (it.target as HTMLInputElement).checked
            props.meta.value = newValue.asValue()
        }
        styledInput(type = InputType.checkBox) {
            css {
                width = 100.pct
            }
            attrs {
                //this.attributes["indeterminate"] = (props.item == null).toString()
                defaultChecked = props.actual.boolean ?: false
                onChangeFunction = handleChange
            }
        }
    }

@JsExport
public val NumberValueChooser: FunctionComponent<ValueChooserProps> =
    functionalComponent("NumberValueChooser") { props ->
        var innerValue by useState(props.actual.string ?: "")
        val keyDown: (Event) -> Unit = { event ->
            if (event.type == "keydown" && event.asDynamic().key == "Enter") {
                innerValue = (event.target as HTMLInputElement).value
                val number = innerValue.toDoubleOrNull()
                if (number == null) {
                    console.error("The input value $innerValue is not a number")
                } else {
                    props.meta.value = number.asValue()
                }
            }
        }
        val handleChange: (Event) -> Unit = {
            innerValue = (it.target as HTMLInputElement).value
        }
        styledInput(type = InputType.number) {
            css {
                width = 100.pct
            }
            attrs {
                value = innerValue
                onKeyDownFunction = keyDown
                onChangeFunction = handleChange
                props.descriptor?.attributes?.get("step").string?.let {
                    step = it
                }
                props.descriptor?.attributes?.get("min").string?.let {
                    min = it
                }
                props.descriptor?.attributes?.get("max").string?.let {
                    max = it
                }
            }
        }
    }

@JsExport
public val ComboValueChooser: FunctionComponent<ValueChooserProps> =
    functionalComponent("ComboValueChooser") { props ->
        var selected by useState(props.actual.string ?: "")
        val handleChange: (Event) -> Unit = {
            selected = (it.target as HTMLSelectElement).value
            props.meta.value = selected.asValue()
        }
        styledSelect {
            css {
                width = 100.pct
            }
            props.descriptor?.allowedValues?.forEach {
                option {
                    +it.string
                }
            }
            attrs {
                this.value = props.actual.string ?: ""
                multiple = false
                onChangeFunction = handleChange
            }
        }
    }

@JsExport
public val ColorValueChooser: FunctionComponent<ValueChooserProps> =
    functionalComponent("ColorValueChooser") { props ->
        val handleChange: (Event) -> Unit = {
            props.meta.value = (it.target as HTMLInputElement).value.asValue()
        }
        styledInput(type = InputType.color) {
            css {
                width = 100.pct
                margin(0.px)
            }
            attrs {
                this.value = props.actual.value?.let { value ->
                    if (value.type == ValueType.NUMBER) Colors.rgbToString(value.int)
                    else value.string
                } ?: "#000000"
                onChangeFunction = handleChange
            }
        }
    }

@JsExport
public val ValueChooser: FunctionComponent<ValueChooserProps> = functionalComponent("ValueChooser") { props ->
    val rawInput by useState(false)

    val descriptor = props.descriptor
    val type = descriptor?.valueTypes?.firstOrNull()

    when {
        rawInput -> child(StringValueChooser, props)
        descriptor?.widgetType == "color" -> child(ColorValueChooser, props)
        descriptor?.widgetType == "multiSelect" -> child(MultiSelectChooser, props)
        descriptor?.widgetType == "range" -> child(RangeValueChooser, props)
        type == ValueType.BOOLEAN -> child(BooleanValueChooser, props)
        type == ValueType.NUMBER -> child(NumberValueChooser, props)
        descriptor?.allowedValues?.isNotEmpty() ?: false -> child(ComboValueChooser, props)
        //TODO handle lists
        else -> child(StringValueChooser, props)
    }
}
