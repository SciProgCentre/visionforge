package space.kscience.visionforge.react

import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.FunctionalComponent
import react.dom.attrs
import react.functionalComponent
import react.useState
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.values.asValue
import styled.styledInput

@JsExport
public val RangeValueChooser: FunctionalComponent<ValueChooserProps> =
    functionalComponent("RangeValueChooser") { props ->
        var innerValue by useState(props.item.string)

        val handleChange: (Event) -> Unit = {
            val newValue = (it.target as HTMLInputElement).value
            props.valueChanged?.invoke(newValue.toDoubleOrNull()?.asValue())
            innerValue = newValue
        }

        styledInput(type = InputType.range) {
            attrs {
                value = innerValue ?: ""
                onChangeFunction = handleChange
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