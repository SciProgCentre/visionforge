package hep.dataforge.vision.react

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.ValueDescriptor
import hep.dataforge.names.Name
import hep.dataforge.values.*
import hep.dataforge.vision.Colors
import hep.dataforge.vision.widgetType
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onKeyDownFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import react.*
import react.dom.defaultValue
import react.dom.option
import styled.styledInput
import styled.styledSelect

public external interface ValueChooserProps : RProps {
        public var item: MetaItem?
    public var descriptor: ValueDescriptor?
    public var valueChanged: ((Value?) -> Unit)?
}

public external interface ValueChooserState : RState {
    public var rawInput: Boolean?
}

@JsExport
public class ValueChooserComponent(props: ValueChooserProps) : RComponent<ValueChooserProps, ValueChooserState>(props) {
    private val element = createRef<HTMLElement>()

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

    private val commit: (Event) -> Unit = { _ ->
        props.valueChanged?.invoke(getValue())
    }

    private val keyDown: (Event) -> Unit = { event ->
        if (event.type == "keydown" && event.asDynamic().key == "Enter") {
            commit(event)
        }
    }

    override fun shouldComponentUpdate(
        nextProps: ValueChooserProps,
        nextState: ValueChooserState
    ): Boolean = nextProps.item !== props.item

    override fun componentDidUpdate(prevProps: ValueChooserProps, prevState: ValueChooserState, snapshot: Any) {
        (element.current as? HTMLInputElement)?.let { element ->
            if (element.type == "checkbox") {
                element.defaultChecked = props.item?.boolean ?: false
            } else {
                element.defaultValue = props.item?.string ?: ""
            }
            element.indeterminate = props.item == null
        }
    }

    private fun RBuilder.stringInput() = styledInput(type = InputType.text) {
        attrs {
            this.defaultValue = props.item?.string ?: ""
            onKeyDownFunction = keyDown
        }
        ref = element
    }

    override fun RBuilder.render() {
        val descriptor = props.descriptor
        val type = descriptor?.type?.firstOrNull()
        when {
            state.rawInput == true -> stringInput()
            descriptor?.widgetType == "color" -> styledInput(type = InputType.color) {
                ref = element
                attrs {
                    this.defaultValue = props.item?.value?.let { value ->
                        if (value.type == ValueType.NUMBER) Colors.rgbToString(value.int)
                        else value.string
                    } ?: "#000000"
                    onChangeFunction = commit
                }
            }
            type == ValueType.BOOLEAN -> {
                styledInput(type = InputType.checkBox) {
                    ref = element
                    attrs {
                        defaultChecked = props.item?.boolean ?: false
                        onChangeFunction = commit
                    }
                }
            }
            type == ValueType.NUMBER -> styledInput(type = InputType.number) {
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
                    defaultValue = props.item?.string ?: ""
                    onKeyDownFunction = keyDown
                }
            }
            descriptor?.allowedValues?.isNotEmpty() ?: false -> styledSelect {
                descriptor!!.allowedValues.forEach {
                    option {
                        +it.string
                    }
                }
                ref = element
                attrs {
                    this.value = props.item?.string ?: ""
                    multiple = false
                    onChangeFunction = commit
                }
            }
            else -> stringInput()
        }
    }
}

internal fun RBuilder.valueChooser(
    name: Name,
    item: MetaItem?,
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
