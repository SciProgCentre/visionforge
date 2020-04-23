package hep.dataforge.vis.editor

import hep.dataforge.js.RFBuilder
import hep.dataforge.js.component
import hep.dataforge.js.state
import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.*
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.plus
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import react.*
import react.dom.*

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

private fun RFBuilder.configEditorItem(props: ConfigEditorProps) {
    var expanded: Boolean by state { true }
    val item = props.root[props.name]
    val descriptorItem: ItemDescriptor? = props.descriptor?.get(props.name)
    val defaultItem = props.default?.get(props.name)
    val actualItem: MetaItem<Meta>? = item ?: defaultItem ?: descriptorItem?.defaultItem()

    val token = props.name.last()?.toString() ?: "Properties"

    var kostyl by state { false }

    fun update() {
        kostyl = !kostyl
    }

    useEffectWithCleanup(listOf(props.root)) {
        props.root.onChange(this) { name, _, _ ->
            if (name == props.name) {
                update()
            }
        }
        return@useEffectWithCleanup { props.root.removeListener(this) }
    }

    val expanderClick: (Event) -> Unit = {
        expanded = !expanded
    }

    val removeClick: (Event) -> Unit = {
        props.root.remove(props.name)
        update()
    }

    when (actualItem) {
        is MetaItem.NodeItem -> {
            div {
                span("tree-caret") {
                    attrs {
                        if (expanded) {
                            classes += "tree-caret-down"
                        }
                        onClickFunction = expanderClick
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
            if (expanded) {
                ul("tree") {
                    val keys = buildSet<NameToken> {
                        (descriptorItem as? NodeDescriptor)?.items?.keys?.forEach {
                            add(NameToken(it))
                        }
                        item?.node?.items?.keys?.let { addAll(it) }
                        defaultItem?.node?.items?.keys?.let { addAll(it) }
                    }

                    keys.forEach { token ->
                        li("tree-item align-middle") {
                            configEditor(props.root, props.name + token, props.descriptor, props.default)
                        }
                    }
                }
            }
        }
        is MetaItem.ValueItem -> {
            div {
                div("d-flex flex-row align-items-center") {
                    div("flex-grow-1 p-1 mr-auto tree-label") {
                        +token
                        attrs {
                            if (item == null) {
                                classes += "tree-label-inactive"
                            }
                        }
                    }
                    div("d-inline-flex") {
                        valueChooser(props.root, props.name, actualItem.value, descriptorItem as? ValueDescriptor)
                    }
                    div("d-inline-flex p-1") {
                        button(classes = "btn btn-link") {
                            +"\u00D7"
                            attrs {
                                if (item == null) {
                                    disabled = true
                                } else {
                                    onClickFunction = removeClick
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

val ConfigEditor: FunctionalComponent<ConfigEditorProps> = component { configEditorItem(it) }

fun RBuilder.configEditor(
    config: Config,
    name: Name = Name.EMPTY,
    descriptor: NodeDescriptor? = null,
    default: Meta? = null
) {
    child(ConfigEditor) {
        attrs {
            this.root = config
            this.name = name
            this.descriptor = descriptor
            this.default = default
        }
    }
}

fun Element.configEditor(config: Config, descriptor: NodeDescriptor? = null, default: Meta? = null) {
    render(this) {
        configEditor(config, Name.EMPTY, descriptor, default)
    }
}

fun RBuilder.configEditor(obj: Configurable, descriptor: NodeDescriptor? = obj.descriptor, default: Meta? = null) {
    configEditor(obj.config, Name.EMPTY, descriptor ?: obj.descriptor, default)
}
