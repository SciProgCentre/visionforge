@file:Suppress("UNUSED_PARAMETER")

package space.kscience.visionforge.compose

import androidx.compose.runtime.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.asList
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.ValueRestriction
import space.kscience.dataforge.meta.descriptors.allowedValues
import space.kscience.visionforge.Colors
import space.kscience.visionforge.widgetType


@Composable
public fun StringValueChooser(
    descriptor: MetaDescriptor?,
    state: EditorPropertyState,
    value: Value?,
    onValueChange: (Value?) -> Unit,
) {
    var stringValue by remember { mutableStateOf(value?.string ?: "") }
    Input(type = InputType.Text) {
        style {
            width(100.percent)
        }
        value(stringValue)
        onKeyDown { event ->
            if (event.type == "keydown" && event.asDynamic().key == "Enter") {
                stringValue = (event.target as HTMLInputElement).value
                onValueChange(stringValue.asValue())
            }
        }
        onChange {
            stringValue = it.target.value
        }
    }
}


@Composable
public fun BooleanValueChooser(
    descriptor: MetaDescriptor?,
    state: EditorPropertyState,
    value: Value?,
    onValueChange: (Value?) -> Unit,
) {
    Input(type = InputType.Checkbox) {
        style {
            width(100.percent)
        }
        //this.attributes["indeterminate"] = (props.item == null).toString()
        checked(value?.boolean ?: false)

        onChange {
            val newValue = it.target.checked
            onValueChange(newValue.asValue())
        }
    }
}

@Composable
public fun NumberValueChooser(
    descriptor: MetaDescriptor?,
    state: EditorPropertyState,
    value: Value?,
    onValueChange: (Value?) -> Unit,
) {
    var innerValue by remember { mutableStateOf(value?.string ?: "") }
    Input(type = InputType.Number) {
        style {
            width(100.percent)
        }
        value(innerValue)
        onKeyDown { event ->
            if (event.type == "keydown" && event.asDynamic().key == "Enter") {
                innerValue = (event.target as HTMLInputElement).value
                val number = innerValue.toDoubleOrNull()
                if (number == null) {
                    console.error("The input value $innerValue is not a number")
                } else {
                    onValueChange(number.asValue())
                }
            }
        }
        onChange {
            innerValue = it.target.value
        }
        descriptor?.attributes?.get("step").number?.let {
            step(it)
        }
        descriptor?.attributes?.get("min").string?.let {
            min(it)
        }
        descriptor?.attributes?.get("max").string?.let {
            max(it)
        }
    }
}


@Composable
public fun ComboValueChooser(
    descriptor: MetaDescriptor?,
    state: EditorPropertyState,
    value: Value?,
    onValueChange: (Value?) -> Unit,
) {
    var selected by remember { mutableStateOf(value?.string ?: "") }
    Select({
        style {
            width(100.percent)
        }
        onChange {
            selected = it.target.value
            onValueChange(selected.asValue())
        }
    }, multiple = false) {
        descriptor?.allowedValues?.forEach {
            Option(it.string, { if (it == value) selected() }) {
                Text(it.string)
            }
        }

    }
}

@Composable
public fun ColorValueChooser(
    descriptor: MetaDescriptor?,
    state: EditorPropertyState,
    value: Value?,
    onValueChange: (Value?) -> Unit,
) {
    Input(type = InputType.Color) {
        style {
            width(100.percent)
            marginAll(0.px)
        }
        value(
            value?.let { value ->
                if (value.type == ValueType.NUMBER) Colors.rgbToString(value.int)
                else value.string
                //else "#" + Color(value.string).getHexString()
            } ?: "#000000"
        )
        onChange {
            onValueChange(it.target.value.asValue())
        }
    }
}


@Composable
public fun MultiSelectChooser(
    descriptor: MetaDescriptor?,
    state: EditorPropertyState,
    value: Value?,
    onValueChange: (Value?) -> Unit,
) {
    Select({
        onChange { event ->
            val newSelected = event.target.selectedOptions.asList()
                .map { (it as HTMLOptionElement).value.asValue() }
            onValueChange(newSelected.asValue())

        }
    }, multiple = true) {
        descriptor?.allowedValues?.forEach { optionValue ->
            Option(optionValue.string, {
                value?.list?.let { if (optionValue in it) selected() }
            }) {
                Text(optionValue.string)
            }
        }

    }
}

@Composable
public fun RangeValueChooser(
    descriptor: MetaDescriptor?,
    state: EditorPropertyState,
    value: Value?,
    onValueChange: (Value?) -> Unit,
) {
    var innerValue by remember { mutableStateOf(value?.double) }
    var rangeDisabled: Boolean by remember { mutableStateOf(state != EditorPropertyState.Defined) }


    FlexRow {
        if (descriptor?.valueRestriction != ValueRestriction.REQUIRED) {
            Input(type = InputType.Checkbox) {
                if (!rangeDisabled) defaultChecked()

                onChange {
                    val checkBoxValue = it.target.checked
                    rangeDisabled = !checkBoxValue
                    onValueChange(
                        if (!checkBoxValue) {
                            null
                        } else {
                            innerValue?.asValue()
                        }
                    )
                }
            }
        }
    }

    Input(type = InputType.Range) {
        style {
            width(100.percent)
        }
        if (rangeDisabled) disabled()
        value(innerValue?.toString() ?: "")
        onChange {
            val newValue = it.target.value
            onValueChange(newValue.toDoubleOrNull()?.asValue())
            innerValue = newValue.toDoubleOrNull()
        }
        descriptor?.attributes?.get("min").string?.let {
            min(it)
        }
        descriptor?.attributes?.get("max").string?.let {
            max(it)
        }
        descriptor?.attributes?.get("step").number?.let {
            step(it)
        }

    }

}

@Composable
public fun ValueChooser(
    descriptor: MetaDescriptor?,
    state: EditorPropertyState,
    value: Value?,
    onValueChange: (Value?) -> Unit,
) {
    val rawInput by remember { mutableStateOf(false) }

    val type = descriptor?.valueTypes?.firstOrNull()

    when {
        rawInput -> StringValueChooser(descriptor, state, value, onValueChange)
        descriptor?.widgetType == "color" -> ColorValueChooser(descriptor, state, value, onValueChange)
        descriptor?.widgetType == "multiSelect" -> MultiSelectChooser(descriptor, state, value, onValueChange)
        descriptor?.widgetType == "range" -> RangeValueChooser(descriptor, state, value, onValueChange)
        type == ValueType.BOOLEAN -> BooleanValueChooser(descriptor, state, value, onValueChange)
        type == ValueType.NUMBER -> NumberValueChooser(descriptor, state, value, onValueChange)
        descriptor?.allowedValues?.isNotEmpty() ?: false -> ComboValueChooser(descriptor, state, value, onValueChange)
        //TODO handle lists
        else -> StringValueChooser(descriptor, state, value, onValueChange)
    }
}