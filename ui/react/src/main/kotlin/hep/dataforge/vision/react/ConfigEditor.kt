package hep.dataforge.vision.react

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.*
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.plus
import hep.dataforge.values.Value
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import react.*
import react.dom.render
import styled.*

interface ConfigEditorItemProps : RProps {

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

private val ConfigEditorItem: FunctionalComponent<ConfigEditorItemProps> = component { props ->
    configEditorItem(props)
}

private fun RFBuilder.configEditorItem(props: ConfigEditorItemProps) {
    var expanded: Boolean by state { true }
    var item: MetaItem<Config>? by state { props.root[props.name] }
    val descriptorItem: ItemDescriptor? = props.descriptor?.get(props.name)
    val defaultItem = props.default?.get(props.name)
    var actualItem: MetaItem<Meta>? by state { item ?: defaultItem ?: descriptorItem?.defaultItem() }

    val token = props.name.last()?.toString() ?: "Properties"

    fun update() {
        item = props.root[props.name]
        actualItem = item ?: defaultItem ?: descriptorItem?.defaultItem()
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

    val valueChanged: (Value?) -> Unit = {
        if (it == null) {
            props.root.remove(props.name)
        } else {
            props.root[props.name] = it
        }
        update()
    }

    val removeClick: (Event) -> Unit = {
        props.root.remove(props.name)
        update()
    }

    when (actualItem) {
        is MetaItem.NodeItem -> {
            styledDiv {
                css {
                    +TreeStyles.treeLeaf
                }
                styledSpan {
                    css {
                        +TreeStyles.treeCaret
                        if (expanded) {
                            +TreeStyles.treeCaredDown
                        }
                    }
                    attrs {
                        onClickFunction = expanderClick
                    }
                }
                styledSpan {
                    css {
                        +TreeStyles.treeLabel
                        if (item == null) {
                            +TreeStyles.treeLabelInactive
                        }
                    }
                    +token
                }
            }
            if (expanded) {
                styledUl {
                    css {
                        +TreeStyles.tree
                    }
                    val keys = buildSet<NameToken> {
                        (descriptorItem as? NodeDescriptor)?.items?.keys?.forEach {
                            add(NameToken(it))
                        }
                        item?.node?.items?.keys?.let { addAll(it) }
                        defaultItem?.node?.items?.keys?.let { addAll(it) }
                    }

                    keys.forEach { token ->
                        styledLi {
                            css {
                                +TreeStyles.treeItem
                            }
                            child(ConfigEditorItem) {
                                attrs {
                                    this.key = props.name.toString()
                                    this.root = props.root
                                    this.name = props.name + token
                                    this.default = props.default
                                    this.descriptor = props.descriptor
                                }
                            }
                            //configEditor(props.root, props.name + token, props.descriptor, props.default)
                        }
                    }
                }
            }
        }
        is MetaItem.ValueItem -> {
            styledDiv {
                css {
                    +TreeStyles.treeLeaf
//                     justifyContent = JustifyContent.flexEnd
                }
                styledDiv {
                    css {
                        +TreeStyles.treeLabel
                    }
                    styledSpan {
                        css {
                            if (item == null) {
                                +TreeStyles.treeLabelInactive
                            }
                        }
                        +token
                    }
                }
                styledDiv {
                    css {
                        +TreeStyles.resizeableInput
                    }
                    valueChooser(
                        props.name,
                        actualItem,
                        descriptorItem as? ValueDescriptor,
                        valueChanged
                    )
                }
                styledButton {
                    css {
                        +TreeStyles.removeButton
                    }
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

interface ConfigEditorProps : RProps {
    var id: Name
    var root: Config
    var default: Meta?
    var descriptor: NodeDescriptor?
}

val ConfigEditor = component<ConfigEditorProps> { props ->
    child(ConfigEditorItem) {
        attrs {
            this.key = ""
            this.root = props.root
            this.name = Name.EMPTY
            this.default = props.default
            this.descriptor = props.descriptor
        }
    }
}

fun Element.configEditor(config: Config, descriptor: NodeDescriptor? = null, default: Meta? = null, key: Any? = null) {
    render(this) {
        child(ConfigEditor) {
            attrs {
                this.key = key?.toString() ?: ""
                this.root = config
                this.descriptor = descriptor
                this.default = default
            }
        }
    }
}

fun RBuilder.configEditor(config: Config, descriptor: NodeDescriptor? = null, default: Meta? = null, key: Any? = null) {
    child(ConfigEditor) {
        attrs {
            this.key = key?.toString() ?: ""
            this.root = config
            this.descriptor = descriptor
            this.default = default
        }
    }
}

fun RBuilder.configEditor(
    obj: Configurable,
    descriptor: NodeDescriptor? = obj.descriptor,
    default: Meta? = null,
    key: Any? = null
) {
    child(ConfigEditor) {
        attrs {
            this.key = key?.toString() ?: ""
            this.root = obj.config
            this.descriptor = descriptor
            this.default = default
        }
    }
}
