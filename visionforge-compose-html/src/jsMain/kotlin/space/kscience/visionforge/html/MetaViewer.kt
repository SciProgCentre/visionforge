package space.kscience.visionforge.html

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.meta.isLeaf
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.lastOrNull
import space.kscience.dataforge.names.plus


@Composable
private fun MetaViewerItem(root: Meta, name: Name, rootDescriptor: MetaDescriptor? = null) {
    var expanded: Boolean by remember { mutableStateOf(true) }
    val item: Meta? = root[name]
    val descriptorItem: MetaDescriptor? = rootDescriptor?.get(name)
    val actualValue = item?.value ?: descriptorItem?.defaultValue
    val actualMeta = item ?: descriptorItem?.defaultNode

    val token = name.lastOrNull()?.toString() ?: ""

    FlexRow(attrs = {
        classes("metaItem")
        style {
            alignItems(AlignItems.Center)
        }
    }) {
        if (actualMeta?.isLeaf == false) {
            Span({
                classes(TreeStyles.treeCaret)
                if (expanded) {
                    classes(TreeStyles.treeCaretDown)
                }
                onClick { expanded = !expanded }
            })
        }

        Span({
            classes(TreeStyles.treeLabel)
            if (item == null) {
                classes(TreeStyles.treeLabelInactive)
            }
        }) {
            Text(token)
        }

        Div {
            A {
                Text(actualValue.toString())
            }
        }
    }
    if (expanded) {
        FlexColumn({
            classes(TreeStyles.tree)
        }) {
            val keys = buildSet {
                descriptorItem?.nodes?.keys?.forEach {
                    add(NameToken(it))
                }
                actualMeta!!.items.keys.let { addAll(it) }
            }

            keys.filter { !it.body.startsWith("@") }.forEach { token ->
                Div({
                    classes(TreeStyles.treeItem)
                }) {
                    MetaViewerItem(root, name + token, rootDescriptor)
                }
            }
        }
    }
}

@Composable
public fun MetaViewer(meta: Meta, descriptor: MetaDescriptor? = null) {
    MetaViewerItem(meta, Name.EMPTY, descriptor)
}
