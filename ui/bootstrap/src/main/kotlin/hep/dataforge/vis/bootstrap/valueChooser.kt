package hep.dataforge.vis.bootstrap

import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.descriptors.ValueDescriptor
import hep.dataforge.meta.get
import hep.dataforge.meta.string
import hep.dataforge.meta.value
import hep.dataforge.names.Name
import hep.dataforge.values.*
import hep.dataforge.vis.Colors
import hep.dataforge.vis.widgetType
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onKeyDownFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.*
import react.dom.div
import react.dom.input
import react.dom.option
import react.dom.select

interface ValueChooserProps : RProps {
    var item: MetaItem<*>?
    var descriptor: ValueDescriptor?
    var valueChanged: ((Value?) -> Unit)?
}

interface ValueChooserState : RState {
    var value: Value?
    var rawInput: Boolean?
}

class ValueChooserComponent(props: ValueChooserProps) : RComponent<ValueChooserProps, ValueChooserState>(props) {
    private val element = createRef<HTMLElement>()

    override fun ValueChooserState.init(props: ValueChooserProps) {
        value = props.item.value
    }

    private fun getValue(): Value? {
        val element = element.current ?: return null//state.element ?: return null
        return when (element) {
            is HTMLInputElement -> if (element.type == "checkbox") {
                if (element.checked) True else False
            } else {
                element.value.asValue()
            }
            is HTMLSelectElement -> element.value.asValue()
            else -> error("Unknown event target: $element")
        }
    }

    private val valueChanged: (Event) -> Unit = { _ ->
        setState {
            value = getValue()
        }
    }

    private val valueChangeAndCommit: (Event) -> Unit = { event ->
        val res = getValue()
        setState {
            value = res
        }
        props.valueChanged?.invoke(res)

    }

    private val keyDown: (Event) -> Unit = { event ->
        if (event is KeyboardEvent && event.key == "Enter") {
            props.valueChanged?.invoke(getValue())
        }
    }

    override fun shouldComponentUpdate(
        nextProps: ValueChooserProps,
        nextState: ValueChooserState
    ): Boolean = nextProps.item !== props.item

    override fun componentDidUpdate(prevProps: ValueChooserProps, prevState: ValueChooserState, snapshot: Any) {
        (element.current as? HTMLInputElement)?.let { element ->
            if (element.type == "checkbox") {
                element.checked = state.value?.boolean ?: false
            } else {
                element.value = state.value?.string ?: ""
            }
            element.indeterminate = state.value == null
        }
//        (state.element as? HTMLSelectElement)?.let { element ->
//            state.value?.let { element.value = it.string }
//        }
    }

    private fun RBuilder.stringInput() = input(type = InputType.text) {
        attrs {
            this.value = state.value?.string ?: ""
            onChangeFunction = valueChanged
            onKeyDownFunction = keyDown
        }
        ref = element
    }

    override fun RBuilder.render() {
        div("align-self-center") {
            val descriptor = props.descriptor
            val type = descriptor?.type?.firstOrNull()
            when {
                state.rawInput == true -> stringInput()
                descriptor?.widgetType == "color" -> input(type = InputType.color) {
                    ref = element
                    attrs {
                        this.value = state.value?.let { value ->
                            if (value.type == ValueType.NUMBER) Colors.rgbToString(value.int)
                            else value.string
                        } ?: "#000000"
                        onChangeFunction = valueChangeAndCommit
                    }
                }
                type == ValueType.BOOLEAN -> {
                    input(type = InputType.checkBox) {
                        ref = element
                        attrs {
                            checked = state.value?.boolean ?: false
                            onChangeFunction = valueChangeAndCommit
                        }
                    }
                }
                type == ValueType.NUMBER -> input(type = InputType.number) {
                    ref = element
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
                        this.value = state.value?.string ?: ""
                        onChangeFunction = valueChanged
                        onKeyDownFunction = keyDown
                    }
                }
                descriptor?.allowedValues?.isNotEmpty() ?: false -> select {
                    descriptor!!.allowedValues.forEach {
                        option {
                            +it.string
                        }
                    }
                    ref = element
                    attrs {
                        this.value = state.value?.string ?: ""
                        multiple = false
                        onChangeFunction = valueChangeAndCommit
                    }
                }
                else -> stringInput()
            }
        }
    }

}

internal fun RBuilder.valueChooser(
    name: Name,
    item: MetaItem<*>?,
    descriptor: ValueDescriptor? = null,
    callback: (Value?) -> Unit
) {
    child(ValueChooserComponent::class) {
        attrs {
            key = name.toString()
            this.item = item
            this.descriptor = descriptor
            this.valueChanged = callback
        }
    }
}
