package space.kscience.visionforge.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    Row(modifier = Modifier.fillMaxWidth()) {
        if (actualMeta?.isLeaf == false) {
            TextButton({ expanded = !expanded }) {
                if (expanded) {
                    Icon(Icons.Filled.ExpandLess, "collapse")
                } else {
                    Icon(Icons.Filled.ExpandMore, "expand")
                }
            }
        }
        Text(token, color = if (item == null) Color.Gray else Color.Unspecified)
        Spacer(Modifier.weight(1f))
        Text(actualValue.toString())
    }
    if (expanded) {
        Column {
            val keys = buildSet {
                descriptorItem?.nodes?.keys?.forEach {
                    add(NameToken(it))
                }
                actualMeta!!.items.keys.let { addAll(it) }
            }

            keys.filter { !it.body.startsWith("@") }.forEach { token ->
                MetaViewerItem(root, name + token, rootDescriptor)
            }
        }
    }
}

@Composable
public fun MetaViewer(meta: Meta, descriptor: MetaDescriptor? = null) {
    MetaViewerItem(meta, Name.EMPTY, descriptor)
}
