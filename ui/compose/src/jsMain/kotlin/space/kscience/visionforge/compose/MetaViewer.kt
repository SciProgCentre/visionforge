package space.kscience.visionforge.compose

import androidx.compose.runtime.*
import kotlinx.html.js.onClickFunction
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.dom.Span
import org.w3c.dom.events.Event
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.isLeaf
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.lastOrNull
import space.kscience.dataforge.names.plus


private val MetaViewerItem: FC<MetaViewerProps> = fc("MetaViewerItem") { props ->
    metaViewerItem(props)
}

@Composable
private fun MetaViewerItem(root: Meta, name: Name, rootDescriptor: MetaDescriptor? = null) {
    var expanded: Boolean by remember { mutableStateOf(true) }
    val item: Meta? = root[name]
    val descriptorItem: MetaDescriptor? = rootDescriptor?.get(name)
    val actualValue = item?.value ?: descriptorItem?.defaultValue
    val actualMeta = item ?: descriptorItem?.defaultNode

    val token = name.lastOrNull()?.toString() ?: props.rootName ?: ""

    val expanderClick: (Event) -> Unit = {
        expanded = !expanded
    }

    FlexRow(attrs = {
        classes("metaItem")
        style {
            alignItems(AlignItems.Center)
        }
    }) {
        if (actualMeta?.isLeaf == false) {
            Span(attrs = {

            })
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
public val MetaViewer: FC<MetaViewerProps> = fc("MetaViewer") { props ->
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
