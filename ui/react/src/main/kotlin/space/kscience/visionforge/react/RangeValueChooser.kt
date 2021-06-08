package space.kscience.visionforge.react

import kotlinx.css.margin
import kotlinx.css.padding
import kotlinx.css.px
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.FunctionalComponent
import react.dom.attrs
import react.functionalComponent
import react.useState
import space.kscience.dataforge.meta.double
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.values.asValue
import styled.css
import styled.styledInput

@JsExport
public val RangeValueChooser: FunctionalComponent<ValueChooserProps> =
    functionalComponent("RangeValueChooser") { props ->
        var innerValue by useState(props.item.double)
        var rangeDisabled: Boolean by useState(props.item == null)

        val handleDisable: (Event) -> Unit = {
            val checkBoxValue = (it.target as HTMLInputElement).checked
            rangeDisabled = !checkBoxValue
            if(!checkBoxValue) {
                props.valueChanged?.invoke(null)
            } else {
                props.valueChanged?.invoke(innerValue?.asValue())
            }
        }

        val handleChange: (Event) -> Unit = {
            val newValue = (it.target as HTMLInputElement).value
            props.valueChanged?.invoke(newValue.toDoubleOrNull()?.asValue())
            innerValue = newValue.toDoubleOrNull()
        }

        flexRow {
            styledInput(type = InputType.checkBox) {
                css{
                    padding(0.px)
                    margin(0.px)
                }
                attrs {
                    defaultChecked = rangeDisabled.not()
                    onChangeFunction = handleDisable
                }
            }

            styledInput(type = InputType.range) {
                attrs {
                    disabled = rangeDisabled
                    value = innerValue?.toString() ?: ""
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
    }