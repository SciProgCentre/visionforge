package hep.dataforge.vis.editor

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.*
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.plus
import hep.dataforge.values.*
import hep.dataforge.vis.widgetType
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import react.RBuilder
import react.RComponent
import react.RProps
import react.dom.*
import react.setState

interface ConfigEditorProps : RProps {
    /**
     * Root config object - always non null
     */
    var root: Config

    /**
     * Full path to the displayed node in [root]. Could be empty
     */
    var name: Name

    /**
     * Root default
     */
    var default: Meta?

    /**
     * Root descriptor
     */
    var descriptor: NodeDescriptor?

}

class ConfigEditorComponent : RComponent<ConfigEditorProps, TreeState>() {

    override fun TreeState.init() {
        expanded = true
    }

    override fun componentDidMount() {
        props.root.onChange(this) { name, _, _ ->
            if (name == props.name) {
                forceUpdate()
            }
        }
    }

    override fun componentWillUnmount() {
        props.root.removeListener(this)
    }

    private val onClick: (Event) -> Unit = {
        setState {
            expanded = !expanded
        }
    }

    private val onValueChange: (Event) -> Unit = {
        val value = when (val t = it.target) {
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
            props.root.setValue(props.name, value)
        } catch (ex: Exception) {
            console.error("Can't set config property ${props.name} to $value")
        }
    }

    private val removeValue: (Event) -> Unit = {
        props.root.remove(props.name)
    }

    //TODO replace by separate components
    private fun RBuilder.valueChooser(value: Value, descriptor: ValueDescriptor?) {
        val type = descriptor?.type?.firstOrNull()
        when {
            type == ValueType.BOOLEAN -> {
                input(type = InputType.checkBox, classes = "float-right") {
                    attrs {
                        defaultChecked = value.boolean
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
                    defaultValue = value.string
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
                    defaultValue = value.string
                    onChangeFunction = onValueChange
                }
            }
            else -> input(type = InputType.text, classes = "float-right") {
                attrs {
                    defaultValue = value.string
                    onChangeFunction = onValueChange
                }
            }
        }

    }


    override fun RBuilder.render() {
        val item = props.root[props.name]
        val descriptorItem: ItemDescriptor? = props.descriptor?.get(props.name)
        val defaultItem = props.default?.get(props.name)
        val actualItem = item ?: defaultItem ?: descriptorItem?.defaultItem()
        val token = props.name.last()?.toString() ?: "Properties"

        when (actualItem) {
            is MetaItem.NodeItem -> {
                div {
                    span("tree-caret") {
                        attrs {
                            if (state.expanded) {
                                classes += "tree-caret-down"
                            }
                            onClickFunction = onClick
                        }
                    }
                    span("tree-label") {
                        +token
                        attrs {
                            if (item == null) {
                                classes += "tree-label-inactive"
                            }
                        }
                    }
                }
                if (state.expanded) {
                    ul("tree") {
                        val keys = buildSet<NameToken> {
                            item?.node?.items?.keys?.let { addAll(it) }
                            defaultItem?.node?.items?.keys?.let { addAll(it) }
                            (descriptorItem as? NodeDescriptor)?.items?.keys?.forEach {
                                add(NameToken(it))
                            }
                        }

                        keys.forEach { token ->
                            li("tree-item") {
                                child(ConfigEditorComponent::class) {
                                    attrs {
                                        this.root = props.root
                                        this.name = props.name + token
                                        this.default = props.default
                                        this.descriptor = props.descriptor
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is MetaItem.ValueItem -> {
                div {
                    div("row") {
                        div("col") {
                            p("tree-label") {
                                +token
                                attrs {
                                    if (item == null) {
                                        classes += "tree-label-inactive"
                                    }
                                }
                            }
                        }
                        div("col") {
                            valueChooser(actualItem.value, descriptorItem as? ValueDescriptor)
                        }
                        div("col-auto") {
                            div("dropleft p-0") {
                                button(classes = "btn btn-outline-primary") {
                                    attrs {
                                        type = ButtonType.button
                                        attributes["data-toggle"] = "dropdown"
                                        attributes["aria-haspopup"] = "true"
                                        attributes["aria-expanded"] = "false"
                                        attributes["data-boundary"] = "viewport"
                                    }
                                    +"\u22ee"
                                }
                                div(classes = "dropdown-menu") {
                                    button(classes = "btn btn-outline dropdown-item") {
                                        +"Info"
                                    }
                                    if (item != null) {
                                        button(classes = "btn btn-outline dropdown-item") {
                                            +"""Clear"""
                                        }
                                        attrs {
                                            onClickFunction = removeValue
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Element.configEditor(config: Config, descriptor: NodeDescriptor? = null, default: Meta? = null) {
    render(this) {
        child(ConfigEditorComponent::class) {
            attrs {
                root = config
                name = Name.EMPTY
                this.descriptor = descriptor
                this.default = default
            }
        }
    }
}

fun RBuilder.configEditor(config: Config, descriptor: NodeDescriptor? = null, default: Meta? = null) {
    div {
        child(ConfigEditorComponent::class) {
            attrs {
                root = config
                name = Name.EMPTY
                this.descriptor = descriptor
                this.default = default
            }
        }
    }
}

fun RBuilder.configEditor(obj: Configurable, descriptor: NodeDescriptor? = obj.descriptor, default: Meta? = null) {
    configEditor(obj.config, descriptor ?: obj.descriptor, default)
}
