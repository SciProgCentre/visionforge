@file:Suppress("UNUSED_PARAMETER")

package space.kscience.visionforge.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardType
import com.eygraber.compose.colorpicker.ColorPicker
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.allowedValues
import space.kscience.visionforge.widgetType
import kotlin.math.roundToInt


@Composable
public fun StringValueChooser(
    descriptor: MetaDescriptor?,
    state: EditorPropertyState,
    value: Value?,
    onValueChange: (Value?) -> Unit,
) {
    var stringValue by remember(value, descriptor) { mutableStateOf(value?.string ?: "") }
    TextField(
        value = stringValue,
        onValueChange = {
            stringValue = it
            onValueChange(it.asValue())
        },
        modifier = Modifier.fillMaxWidth()
    )
}


@Composable
public fun BooleanValueChooser(
    descriptor: MetaDescriptor?,
    state: EditorPropertyState,
    value: Value?,
    onValueChange: (Value?) -> Unit,
) {
    var innerValue by remember(value, descriptor) {
        mutableStateOf(
            value?.boolean ?: descriptor?.defaultValue?.boolean ?: false
        )
    }

    Button(
        onClick = {
            innerValue = !innerValue
            onValueChange(innerValue.asValue())
        },
        colors = if (innerValue) ButtonDefaults.buttonColors(Color.Green) else ButtonDefaults.buttonColors(Color.Gray)
    ) {
        if (innerValue) {
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
    var stringValue by remember(value, descriptor) { mutableStateOf(value?.string ?: descriptor?.defaultValue?.string) }

    TextField(
        value = stringValue ?: "",
        onValueChange = { newValue ->
            stringValue = newValue
            newValue.toDoubleOrNull()?.let { onValueChange(it.asValue()) }
        },
        isError = stringValue?.toDoubleOrNull() != null,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
    )
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
public fun ComboValueChooser(
    descriptor: MetaDescriptor?,
    state: EditorPropertyState,
    value: Value?,
    onValueChange: (Value?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember(value, descriptor) { mutableStateOf(value?.string ?: "") }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            descriptor?.allowedValues?.forEach { item: Value ->
                DropdownMenuItem(
                    onClick = {
                        selected = item.string
                        expanded = false
                        onValueChange(selected.asValue())
                    }
                ) {
                    Text(item.string)
                }
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
    //var innerValue by remember { mutableStateOf(value ?: descriptor?.defaultValue) }

    Box(Modifier.fillMaxWidth()) {
        ColorPicker(Modifier.fillMaxWidth()) {
            if (it.isSpecified) {
                onValueChange(it.toArgb().asValue())
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
public fun MultiSelectChooser(
    descriptor: MetaDescriptor?,
    state: EditorPropertyState,
    value: Value?,
    onValueChange: (Value?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selected: Set<Value> by remember(value) {
        mutableStateOf(value?.list?.toSet() ?: emptySet<Value>())
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = selected.joinToString(prefix = "[", postfix = "]"),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            descriptor?.allowedValues?.forEach { item: Value ->
                val currentlySelected = item in selected

                DropdownMenuItem(
                    onClick = {
                        selected = if (currentlySelected) {
                            selected - item
                        } else {
                            selected + item
                        }
                        onValueChange(selected.asValue())
                    }
                ) {
                    if (currentlySelected) {
                        Icon(Icons.Default.CheckBox, "checked")
                    } else {
                        Icon(Icons.Default.CheckBoxOutlineBlank, "checked")
                    }
                    Text(item.string)
                }
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
    val min by derivedStateOf {
        descriptor?.attributes?.get("min").float ?: 0f
    }

    val max by derivedStateOf {
        descriptor?.attributes?.get("max").float ?: 0f
    }

    val step by derivedStateOf {
        descriptor?.attributes?.get("step").float ?: 0.1f
    }

    Slider(
        value = innerValue?.toFloat() ?: 0f,
        onValueChange = {
            innerValue = it
            onValueChange(it.asValue())
        },
        valueRange = min..max,
        steps = ((max - min) / step).roundToInt(),
        modifier = Modifier.fillMaxWidth()
    )
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