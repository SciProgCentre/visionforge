@file:Suppress("UNUSED_PARAMETER")

package space.kscience.visionforge.compose

import androidx.compose.runtime.*
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.asList
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
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
    var stringValue by remember(value, descriptor) { mutableStateOf(value?.string ?: "") }
    Input(type = InputType.Text) {
        classes("w-100")
        value(stringValue)
        onChange { event ->
            stringValue = event.value
        }
        onInput { event ->
            stringValue = event.value
            onValueChange(event.value.asValue())
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
    val uid = remember { "checkbox[${UUID.generateUUID().toString(false)}]" }
    var innerValue by remember(value, descriptor) {
        mutableStateOf(
            value?.boolean ?: descriptor?.defaultValue?.boolean
        )
    }

    Input(type = InputType.Checkbox) {
        classes("btn-check")
        checked(innerValue ?: false)
        autoComplete(AutoComplete.off)
        id(uid)

        onInput { event ->
            innerValue = event.value
            onValueChange(event.value.asValue())
        }
    }
    Label(uid, attrs = { classes("btn", "btn-sm", "btn-outline-secondary", "w-100") }) {
        if (innerValue == true) {
            Text("On")
        } else {
            Text("Off")
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
    var innerValue by remember(value, descriptor) { mutableStateOf(value?.number ?: descriptor?.defaultValue?.number) }
    Input(type = InputType.Number) {
        classes("w-100")

        value(innerValue ?: descriptor?.defaultValue?.number ?: 0.0)

        onChange { event ->
            innerValue = event.value
        }
        onInput { event ->
            innerValue = event.value
            onValueChange(event.value?.asValue())
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
    var selected by remember(value, descriptor) { mutableStateOf(value?.string ?: "") }
    Select({
        classes("w-100")

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
    var innerValue by remember { mutableStateOf<String?>(value?.string ?: descriptor?.defaultValue?.string) }

    Input(type = InputType.Color) {
        classes("w-100")

        value(
            value?.let { value ->
                if (value.type == ValueType.NUMBER) Colors.rgbToString(value.int)
                else value.string
                //else "#" + Color(value.string).getHexString()
            } ?: "#000000"
        )
        onChange { event ->
            innerValue = event.value
        }
        onInput { event ->
            innerValue = event.value
            onValueChange(event.value.asValue())
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
        classes("w-100","form-select")
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
    var innerValue by remember(value, descriptor) { mutableStateOf(value?.number ?: descriptor?.defaultValue?.number) }

    Input(type = InputType.Range) {
        classes("w-100", "form-range")

        value(innerValue?.toString() ?: "")
        onInput { event ->
            innerValue = event.value
        }
        onChange { event ->
            innerValue = event.value
            onValueChange(innerValue?.asValue())
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