package hep.dataforge.vis.editor

import hep.dataforge.meta.Config
import hep.dataforge.meta.descriptors.ValueDescriptor
import hep.dataforge.meta.get
import hep.dataforge.meta.setValue
import hep.dataforge.meta.string
import hep.dataforge.names.Name
import hep.dataforge.values.*
import hep.dataforge.vis.widgetType
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import react.RBuilder
import react.dom.*

internal fun RBuilder.valueChooser(root: Config, name: Name, value: Value, descriptor: ValueDescriptor?) {
    val onValueChange: (Event) -> Unit = {
        val res = when (val t = it.target) {
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

    div() {
        val type = descriptor?.type?.firstOrNull()
        when {
            type == ValueType.BOOLEAN -> {
                input(type = InputType.checkBox) {
                    attrs {
                        checked = value.boolean
                        onChangeFunction = onValueChange
                    }
                }
            }
            type == ValueType.NUMBER -> input(type = InputType.number, classes = "form-control w-100") {
                attrs {
                    descriptor.attributes["step"].string?.let {
                        step = it
                    }
                    descriptor.attributes["min"].string?.let {
                        min = it
                    }
                    descriptor.attributes["max"].string?.let {
                        max = it
                    }
                    this.defaultValue = value.string
                    onChangeFunction = onValueChange
                }
            }
            descriptor?.allowedValues?.isNotEmpty() ?: false -> select (classes = "w-100") {
                descriptor!!.allowedValues.forEach {
                    option {
                        +it.string
                    }
                }
                attrs {
                    multiple = false
                    onChangeFunction = onValueChange
                }
            }
            descriptor?.widgetType == "color" -> input(type = InputType.color) {
                attrs {
                    this.value = value.string
                    onChangeFunction = onValueChange
                }
            }
            else -> input(type = InputType.text, classes = "form-control w-100") {
                attrs {
                    this.value = value.string
                    onChangeFunction = onValueChange
                }
            }
        }
    }
}