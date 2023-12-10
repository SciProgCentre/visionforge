package space.kscience.visionforge.react

import kotlinx.css.pct
import kotlinx.css.width
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.FC
import react.dom.attrs
import react.fc
import react.useState
import space.kscience.dataforge.meta.asValue
import space.kscience.dataforge.meta.descriptors.ValueRestriction
import space.kscience.dataforge.meta.double
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.string
import styled.css
import styled.styledInput

@JsExport
public val RangeValueChooser: FC<ValueChooserProps> = fc("RangeValueChooser") { props ->
    var innerValue by useState(props.value?.double)
    var rangeDisabled: Boolean by useState(props.state != EditorPropertyState.Defined)

    val handleDisable: (Event) -> Unit = {
        val checkBoxValue = (it.target as HTMLInputElement).checked
        rangeDisabled = !checkBoxValue
        props.onValueChange(
            if (!checkBoxValue) {
                null
            } else {
                innerValue?.asValue()
            }
        )
    }

    val handleChange: (Event) -> Unit = {
        val newValue = (it.target as HTMLInputElement).value
        props.onValueChange(newValue.toDoubleOrNull()?.asValue())
        innerValue = newValue.toDoubleOrNull()
    }

    flexRow {
        if (props.descriptor?.valueRestriction != ValueRestriction.REQUIRED) {
            styledInput(type = InputType.checkBox) {
                attrs {
                    defaultChecked = rangeDisabled.not()
                    onChangeFunction = handleDisable
                }
            }
        }

        styledInput(type = InputType.range) {
            css {
                width = 100.pct
            }
            attrs {
                disabled = rangeDisabled
                value = innerValue?.toString() ?: ""
//                onChangeFunction = handleChange
                onInputFunction = handleChange
                val minValue = props.descriptor?.attributes?.get("min").string
                minValue?.let {
                    min = it
                }
                val maxValue = props.descriptor?.attributes?.get("max").string
                maxValue?.let {
                    max = it
                }
                props.descriptor?.attributes?.get("step").string?.let {
                    step = it
                }
            }
        }
    }
}