package space.kscience.visionforge.react

import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.*
import react.dom.a
import react.dom.attrs
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaItemNode
import space.kscience.dataforge.meta.MetaItemValue
import space.kscience.dataforge.meta.descriptors.ItemDescriptor
import space.kscience.dataforge.meta.descriptors.NodeDescriptor
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.lastOrNull
import space.kscience.dataforge.names.plus
import styled.*

public external interface MetaViewerProps : RProps {
    /**
     * Root meta
     */
    public var root: Meta

    /**
     * The title of root node
     */
    public var rootName: String?

    /**
     * Full path to the displayed node in [root]. Could be empty
     */
    public var name: Name

    /**
     * Root descriptor
     */
    public var descriptor: NodeDescriptor?
}

private val MetaViewerItem: FunctionalComponent<MetaViewerProps> = functionalComponent("MetaViewerItem") { props ->
    metaViewerItem(props)
}

private fun RBuilder.metaViewerItem(props: MetaViewerProps) {
    var expanded: Boolean by useState { true }
    val item = props.root[props.name]
    val descriptorItem: ItemDescriptor? = props.descriptor?.get(props.name)
    val actualItem = item ?: descriptorItem?.defaultValue

    val token = props.name.lastOrNull()?.toString() ?: props.rootName ?: ""

    val expanderClick: (Event) -> Unit = {
        expanded = !expanded
    }

    when (actualItem) {
        is MetaItemNode -> {
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
                        actualItem.node.items.keys.let { addAll(it) }
                    }

                    keys.filter { !it.body.startsWith("@") }.forEach { token ->
                        styledLi {
                            css {
                                +TreeStyles.treeItem
                            }
                            child(MetaViewerItem) {
                                attrs {
                                    this.key = props.name.toString()
                                    this.root = props.root
                                    this.name = props.name + token
                                    this.descriptor = props.descriptor
                                }
                            }
                            //configEditor(props.root, props.name + token, props.descriptor, props.default)
                        }
                    }
                }
            }
        }
        is MetaItemValue -> {
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
                    a {
                        +actualItem.value.toString()
                    }
                }
            }
        }
    }
}

@JsExport
public val MetaViewer:FunctionalComponent<MetaViewerProps>  = functionalComponent<MetaViewerProps>("MetaViewer") { props ->
    child(MetaViewerItem) {
        attrs {
            this.key = ""
            this.root = props.root
            this.name = Name.EMPTY
            this.descriptor = props.descriptor
        }
    }
}

public fun RBuilder.metaViewer(meta: Meta, descriptor: NodeDescriptor? = null, key: Any? = null) {
    child(MetaViewer) {
        attrs {
            this.key = key?.toString() ?: ""
            this.root = meta
            this.descriptor = descriptor
        }
    }
}
