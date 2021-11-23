package space.kscience.visionforge.react

import kotlinx.css.Align
import kotlinx.css.alignItems
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.*
import react.dom.a
import react.dom.attrs
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.isLeaf
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.lastOrNull
import space.kscience.dataforge.names.plus
import styled.css
import styled.styledDiv
import styled.styledSpan

public external interface MetaViewerProps : Props {
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
    public var descriptor: MetaDescriptor?
}

private val MetaViewerItem: FunctionComponent<MetaViewerProps> = functionComponent("MetaViewerItem") { props ->
    metaViewerItem(props)
}

private fun RBuilder.metaViewerItem(props: MetaViewerProps) {
    var expanded: Boolean by useState { true }
    val item = props.root[props.name]
    val descriptorItem: MetaDescriptor? = props.descriptor?.get(props.name)
    val actualValue = item?.value ?: descriptorItem?.defaultValue
    val actualMeta = item ?: descriptorItem?.defaultNode

    val token = props.name.lastOrNull()?.toString() ?: props.rootName ?: ""

    val expanderClick: (Event) -> Unit = {
        expanded = !expanded
    }

    flexRow {
        css {
            alignItems = Align.center
        }
        if (actualMeta?.isLeaf == false) {
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
        styledDiv {
            a {
                +actualValue.toString()
            }
        }
    }
    if (expanded) {
        flexColumn {
            css {
                +TreeStyles.tree
            }
            val keys = buildSet {
                descriptorItem?.children?.keys?.forEach {
                    add(NameToken(it))
                }
                actualMeta!!.items.keys.let { addAll(it) }
            }

            keys.filter { !it.body.startsWith("@") }.forEach { token ->
                styledDiv {
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

@JsExport
public val MetaViewer: FunctionComponent<MetaViewerProps> = functionComponent("MetaViewer") { props ->
    child(MetaViewerItem) {
        attrs {
            this.key = ""
            this.root = props.root
            this.name = Name.EMPTY
            this.descriptor = props.descriptor
        }
    }
}

public fun RBuilder.metaViewer(meta: Meta, descriptor: MetaDescriptor? = null, key: Any? = null) {
    child(MetaViewer) {
        attrs {
            this.key = key?.toString() ?: ""
            this.root = meta
            this.descriptor = descriptor
        }
    }
}
