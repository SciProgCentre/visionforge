package hep.dataforge.vis.editor

import hep.dataforge.meta.descriptors.ValueDescriptor
import hep.dataforge.meta.get
import hep.dataforge.meta.string
import hep.dataforge.values.*
import hep.dataforge.vis.widgetType
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import react.RProps
import react.dom.div
import react.dom.input
import react.dom.option
import react.dom.select
import react.functionalComponent

interface ValueChooserProps : RProps {
    var value: Value
    var descriptor: ValueDescriptor?
    var valueChanged: (Value?) -> Unit
}

val ValueChooser = functionalComponent<ValueChooserProps> { props ->
//    var state by initState {props.value }
    val descriptor = props.descriptor

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
//        state = res
        props.valueChanged(res)
    }

    div {
        val type = descriptor?.type?.firstOrNull()
        when {
            type == ValueType.BOOLEAN -> {
                input(type = InputType.checkBox, classes = "float-right") {
                    attrs {
                        checked = props.value.boolean
                        onChangeFunction = onValueChange
                    }
                }
            }
            type == ValueType.NUMBER -> input(type = InputType.number, classes = "float-right") {
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
                    this.value = props.value.string
                    onChangeFunction = onValueChange
                }
            }
            descriptor?.allowedValues?.isNotEmpty() ?: false -> select("float-right") {
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
            descriptor?.widgetType == "color" -> input(type = InputType.color, classes = "float-right") {
                attrs {
                    this.value = props.value.string
                    onChangeFunction = onValueChange
                }
            }
            else -> input(type = InputType.text, classes = "float-right") {
                attrs {
                    this.value = props.value.string
                    onChangeFunction = onValueChange
                }
            }
        }
    }

}