package hep.dataforge.vis.editor

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.meta.descriptors.defaultItem
import hep.dataforge.meta.descriptors.get
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.plus
import hep.dataforge.values.asValue
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
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

    var listen: Boolean
}

class ConfigEditorComponent : RComponent<ConfigEditorProps, TreeState>() {

    override fun TreeState.init() {
        expanded = false
    }

    override fun componentDidMount() {
        if (props.listen) {
            props.root.onChange(this) { name, _, _ ->
                if (name == props.name) {
                    forceUpdate()
                }
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


    override fun RBuilder.render() {
        val item = props.root[props.name]
        val descriptorItem = props.descriptor?.get(props.name)
        val defaultItem = props.default?.get(props.name)
        val actualItem = item ?: defaultItem ?: descriptorItem?.defaultItem()
        val token = props.name.last()

        div("d-inline-block text-truncate") {
            when (actualItem) {
                null -> {
                }
                is MetaItem.ValueItem -> {
                    i("tree-caret") { }
                }
                is MetaItem.NodeItem -> {
                    i("tree-caret fa fa-caret-right") {
                        attrs {
                            if (state.expanded) {
                                classes += "rotate"
                            }
                            onClickFunction = onClick
                        }
                    }
                }
            }
            label("tree-label") {
                +token.toString()
                attrs {
                    if (item == null) {
                        classes += "tree-label-inactive"
                    }
                }
            }

            if (actualItem is MetaItem.NodeItem && state.expanded) {
                ul("tree") {
                    val keys = buildList<NameToken> {
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
                                    root = props.root
                                    name = props.name + token
                                    this.default = props.default
                                    this.descriptor = props.descriptor
                                    listen = false
                                }
                            }
                        }
                    }
                }
            } else if (actualItem is MetaItem.ValueItem) {
                div("row") {
                    div("col") {
                        label("tree-label") {
                            +token.toString()
                        }
                    }
                    div("col") {
                        input(type = InputType.text) {
                            attrs {
                                value = actualItem.value.toString()
                                onChangeFunction = {
                                    try {
                                        props.root.setValue(props.name, value.asValue())
                                    } catch (ex: Exception) {
                                        console.error("Can't set config property $name to $value")
                                    }
                                }
                            }
                        }
                        //+actualItem.value.toString()
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
                listen = true
            }
        }
    }
}

fun RBuilder.configEditor(config: Config, descriptor: NodeDescriptor? = null, default: Meta? = null) {
    child(ConfigEditorComponent::class) {
        attrs {
            root = config
            name = Name.EMPTY
            this.descriptor = descriptor
            this.default = default
            listen = true
        }
    }
}
