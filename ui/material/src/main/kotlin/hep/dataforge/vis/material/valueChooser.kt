package hep.dataforge.vis.material

import hep.dataforge.meta.Config
import hep.dataforge.meta.descriptors.ValueDescriptor
import hep.dataforge.meta.get
import hep.dataforge.meta.number
import hep.dataforge.meta.setValue
import hep.dataforge.names.Name
import hep.dataforge.values.*
import hep.dataforge.vis.widgetType
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onKeyDownFunction
import materialui.components.input.input
import materialui.components.select.select
import materialui.components.slider.slider
import materialui.components.switches.switch
import materialui.components.textfield.textField
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.RBuilder
import react.dom.option

internal fun RBuilder.valueChooser(root: Config, name: Name, value: Value, descriptor: ValueDescriptor?) {
    val onValueChange: (Event) -> Unit = { event ->
        if (event !is KeyboardEvent || event.key == "Enter") {
            val res = when (val t = event.target) {
                // (it.target as HTMLInputElement).value
                is HTMLInputElement -> if (t.type == "checkbox") {
                    if (t.checked) True else False
                } else {
                    t.value.asValue()
                }
                is HTMLSelectElement -> t.value.asValue()
                else -> error("Unknown event target: $t")
            }

            try {
                root.setValue(name, res)
            } catch (ex: Exception) {
                console.error("Can't set config property ${name} to $res")
            }
        }
    }


    val type = descriptor?.type?.firstOrNull()
    when {
        descriptor?.widgetType == "slider" -> slider {
            attrs {
                descriptor.attributes["step"].number?.let {
                    step = it
                }
                descriptor.attributes["min"].number?.let {
                    min = it
                }
                descriptor.attributes["max"].number?.let {
                    max = it
                }
                this.defaultValue = value.number
                onChangeFunction = onValueChange
            }
        }
        descriptor?.widgetType == "color" -> input {
            attrs {
                fullWidth = true
                this.type = InputType.color
                this.value = value.string
                onChangeFunction = onValueChange
            }
        }

        type == ValueType.BOOLEAN -> switch {
            attrs {
                defaultChecked = value.boolean
                onChangeFunction = onValueChange
            }
        }

        type == ValueType.NUMBER -> textField {
            attrs {
                fullWidth = true
                this.type = InputType.number
                defaultValue = value.string
                onChangeFunction = onValueChange
                //onKeyDownFunction = onValueChange
            }
        }
        descriptor?.allowedValues?.isNotEmpty() ?: false -> select {
            descriptor!!.allowedValues.forEach {
                option {
                    +it.string
                }
            }
            attrs {
                fullWidth = true
                multiple = false
                onChangeFunction = onValueChange
            }
        }
        else -> textField {
            attrs {
                this.type = InputType.text
                fullWidth = true
                this.defaultValue = value.string
                //onFocusOutFunction = onValueChange
                onKeyDownFunction = onValueChange
            }
        }
    }

}