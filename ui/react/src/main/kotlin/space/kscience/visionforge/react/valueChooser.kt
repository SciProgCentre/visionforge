package space.kscience.visionforge.react

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
import space.kscience.dataforge.meta.descriptors.ValueDescriptor
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.values.*
import space.kscience.visionforge.Colors
import space.kscience.visionforge.widgetType
import styled.styledInput
import styled.styledSelect

public external interface ValueChooserProps : RProps {
    public var item: MetaItem?
    public var descriptor: ValueDescriptor?
    public var nullable: Boolean?
    public var valueChanged: ((Value?) -> Unit)?
}

@JsExport
public val StringValueChooser: FunctionalComponent<ValueChooserProps> =
    functionalComponent("StringValueChooser") { props ->
        var value by useState(props.item.string ?: "")
        val keyDown: (Event) -> Unit = { event ->
            if (event.type == "keydown" && event.asDynamic().key == "Enter") {
                value = (event.target as HTMLInputElement).value
                if (value != props.item.string) {
                    props.valueChanged?.invoke(value.asValue())
                }
            }
        }
        val handleChange: (Event) -> Unit = {
            value = (it.target as HTMLInputElement).value
        }
        styledInput(type = InputType.text) {
            attrs {
                this.value = value
                onKeyDownFunction = keyDown
                onChangeFunction = handleChange
            }
        }
    }

@JsExport
public val BooleanValueChooser: FunctionalComponent<ValueChooserProps> =
    functionalComponent("BooleanValueChooser") { props ->
        val handleChange: (Event) -> Unit = {
            val newValue = (it.target as HTMLInputElement).checked
            props.valueChanged?.invoke(newValue.asValue())
        }
        styledInput(type = InputType.checkBox) {
            attrs {
                //this.attributes["indeterminate"] = (props.item == null).toString()
                defaultChecked = props.item.boolean ?: false
                onChangeFunction = handleChange
            }
        }
    }

@JsExport
public val NumberValueChooser: FunctionalComponent<ValueChooserProps> =
    functionalComponent("NumberValueChooser") { props ->
        var innerValue by useState(props.item.string ?: "")
        val keyDown: (Event) -> Unit = { event ->
            if (event.type == "keydown" && event.asDynamic().key == "Enter") {
                innerValue = (event.target as HTMLInputElement).value
                val number = innerValue.toDoubleOrNull()
                if (number == null) {
                    console.error("The input value $innerValue is not a number")
                } else {
                    props.valueChanged?.invoke(number.asValue())
                }
            }
        }
        val handleChange: (Event) -> Unit = {
            innerValue = (it.target as HTMLInputElement).value
        }
        styledInput(type = InputType.number) {
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
public val ComboValueChooser: FunctionalComponent<ValueChooserProps> =
    functionalComponent("ComboValueChooser") { props ->
        var selected by useState(props.item.string ?: "")
        val handleChange: (Event) -> Unit = {
            selected = (it.target as HTMLSelectElement).value
            props.valueChanged?.invoke(selected.asValue())
        }
        styledSelect {
            props.descriptor?.allowedValues?.forEach {
                option {
                    +it.string
                }
            }
            attrs {
                this.value = props.item?.string ?: ""
                multiple = false
                onChangeFunction = handleChange
            }
        }
    }

@JsExport
public val ColorValueChooser: FunctionalComponent<ValueChooserProps> =
    functionalComponent("ColorValueChooser") { props ->
        var value by useState(
            props.item.value?.let { value ->
                if (value.type == ValueType.NUMBER) Colors.rgbToString(value.int)
                else value.string
            } ?: "#000000"
        )
        val handleChange: (Event) -> Unit = {
            value = (it.target as HTMLInputElement).value
            props.valueChanged?.invoke(value.asValue())
        }
        styledInput(type = InputType.color) {
            attrs {
                this.value = value
                onChangeFunction = handleChange
            }
        }
    }

@JsExport
public val ValueChooser: FunctionalComponent<ValueChooserProps> = functionalComponent("ValueChooser") { props ->
    val rawInput by useState(false)

    val descriptor = props.descriptor
    val type = descriptor?.type?.firstOrNull()

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

internal fun RBuilder.valueChooser(
    name: Name,
    item: MetaItem?,
    descriptor: ValueDescriptor? = null,
    callback: (Value?) -> Unit,
) {
    child(ValueChooser) {
        attrs {
            key = name.toString()
            this.item = item
            this.descriptor = descriptor
            this.valueChanged = callback
        }
    }
}
