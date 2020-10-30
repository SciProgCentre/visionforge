package hep.dataforge.vision.react

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.*
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.lastOrNull
import hep.dataforge.names.plus
import hep.dataforge.values.Value
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import react.*
import react.dom.render
import styled.*

public external interface ConfigEditorItemProps : RProps {

    /**
     * Root config object - always non null
     */
    public var root: Config

    /**
     * Full path to the displayed node in [root]. Could be empty
     */
    public var name: Name

    /**
     * Root default
     */
    public var default: Meta?

    /**
     * Root descriptor
     */
    public var descriptor: NodeDescriptor?
}

private val ConfigEditorItem: FunctionalComponent<ConfigEditorItemProps> = functionalComponent("ConfigEditorItem") { props ->
    configEditorItem(props)
}

private fun RBuilder.configEditorItem(props: ConfigEditorItemProps) {
    var expanded: Boolean by useState { true }
    var item: MetaItem<Config>? by useState { props.root[props.name] }
    val descriptorItem: ItemDescriptor? = props.descriptor?.get(props.name)
    val defaultItem = props.default?.get(props.name)
    var actualItem: MetaItem<Meta>? by useState { item ?: defaultItem ?: descriptorItem?.defaultItem() }

    val token = props.name.lastOrNull()?.toString() ?: "Properties"

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
                    val keys = buildSet {
                        (descriptorItem as? NodeDescriptor)?.items?.keys?.forEach {
                            add(NameToken(it))
                        }
                        item?.node?.items?.keys?.let { addAll(it) }
                        defaultItem?.node?.items?.keys?.let { addAll(it) }
                    }

                    keys.filter { !it.body.startsWith("@") }.forEach { token ->
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

public external interface ConfigEditorProps : RProps {
    public var id: Name
    public var root: Config
    public var default: Meta?
    public var descriptor: NodeDescriptor?
}

@JsExport
public val ConfigEditor: FunctionalComponent<ConfigEditorProps> = functionalComponent("ConfigEditor") { props ->
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

public fun Element.configEditor(config: Config, descriptor: NodeDescriptor? = null, default: Meta? = null, key: Any? = null) {
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

public fun RBuilder.configEditor(config: Config, descriptor: NodeDescriptor? = null, default: Meta? = null, key: Any? = null) {
    child(ConfigEditor) {
        attrs {
            this.key = key?.toString() ?: ""
            this.root = config
            this.descriptor = descriptor
            this.default = default
        }
    }
}

public fun RBuilder.configEditor(
    obj: Configurable,
    descriptor: NodeDescriptor? = obj.descriptor,
    default: Meta? = null,
    key: Any? = null
) = configEditor(obj.config,descriptor, default, key)
