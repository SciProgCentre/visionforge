package space.kscience.visionforge.editor

import javafx.beans.binding.Binding
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.ListBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.names.*
import space.kscience.dataforge.values.Value
import tornadofx.*

/**
 * A display for meta and descriptor
 */
public class FXMetaModel<M : Meta>(
    public val root: M,
    public val rootDescriptor: MetaDescriptor?,
    public val nodeName: Name,
    public val title: String = nodeName.lastOrNull()?.toString() ?: "Meta"
) : Comparable<FXMetaModel<*>> {

    private val existingNode = SimpleObjectProperty<Meta>(root[nodeName])

    public val children: ListBinding<FXMetaModel<M>> = object : ListBinding<FXMetaModel<M>>() {
        override fun computeValue(): ObservableList<FXMetaModel<M>> {
            val nodeKeys = existingNode.get().items.keys
            val descriptorKeys = descriptor?.children?.keys?.map { NameToken(it) } ?: emptySet()
            return (nodeKeys + descriptorKeys).map {
                FXMetaModel(
                    root,
                    rootDescriptor,
                    nodeName + it
                )
            }.filter(filter).asObservable()
        }
    }

    init {
        //add listener to the root node if possible
        if (root is ObservableMeta) {
            root.onChange(this) { changed ->
                if (changed.startsWith(nodeName)) {
                    if (nodeName.length == changed.length) existingNode.set(root[nodeName])
                    else if (changed.length == nodeName.length + 1) children.invalidate()
                }
            }
        }
    }

    public val descriptor: MetaDescriptor? = rootDescriptor?.get(nodeName)

    public val existsProperty: BooleanBinding = existingNode.isNotNull

    public val exists: Boolean by existsProperty

    public val valueProperty: Binding<Value?> = existingNode.objectBinding {
        existingNode.get().value ?: descriptor?.defaultValue
    }

    override fun compareTo(other: FXMetaModel<*>): Int = if (this.exists == other.exists) {
        this.nodeName.toString().compareTo(other.nodeName.toString())
    } else {
        this.exists.compareTo(other.exists)
    }

    public companion object {
        private val filter: (FXMetaModel<*>) -> Boolean = { cfg ->
            !(cfg.descriptor?.attributes?.get(MutableMetaEditor.NO_CONFIGURATOR_TAG)?.boolean ?: false)
        }

        public fun <M : Meta> root(
            node: M,
            descriptor: MetaDescriptor? = null,
            rootName: String = "root"
        ): FXMetaModel<M> = FXMetaModel(node, descriptor, Name.EMPTY, title = rootName)
    }

//    /**
//     * A descriptor that could be manually set to the node
//     */
//    private val innerDescriptorProperty = SimpleObjectProperty(descriptorValue)
//
//    /**
//     * Actual descriptor which holds value inferred from parrent
//     */
//    val descriptorProperty: ObjectBinding<MetaDescriptor?> = objectBinding(innerDescriptorProperty) {
//        value ?: parent?.descriptor?.get(this@FXMeta.name.body)
//    }
//
//    val descriptor: MetaDescriptor? by descriptorProperty
//
//    private val innerNodeProperty = SimpleObjectProperty(nodeValue)
//
//    val nodeProperty: ObjectBinding<M> = objectBinding(innerNodeProperty) {
//        value ?: parent?.node?.get(this@FXMeta.name)
//    }
//
//    val node by nodeProperty
//
//    val hasValue: ObservableBooleanValue = nodeProperty.booleanBinding { it != null }
//
//    private val filter: (FXMeta<M>) -> Boolean = { cfg ->
//        !(cfg.descriptor?.attributes?.get(MutableMetaEditor.NO_CONFIGURATOR_TAG)?.boolean ?: false)
//    }
//
//    val children: ListBinding<FXMeta<M>> = object : ListBinding<FXMeta<M>>() {
//
//        init {
//            bind(nodeProperty, descriptorProperty)
//
//            val listener: Meta.(Name) -> Unit = { name ->
//                if (name.length == 1) invalidate()
//            }
//
//            (node as? ObservableMeta)?.onChange(this, listener)
//
//            nodeProperty.addListener { _, oldValue, newValue ->
//                if (newValue == null) {
//                    (oldValue as? ObservableMeta)?.removeListener(this)
//                }
//
//                if (newValue is ObservableMeta) {
//                    newValue.onChange(this, listener)
//                }
//            }
//        }
//
//        override fun computeValue(): ObservableList<FXMeta<M>> {
//            val nodeKeys = node?.items?.keys?.toSet() ?: emptySet()
//            val descriptorKeys = descriptor?.children?.keys?.map { NameToken(it) } ?: emptyList()
//            val keys: Set<NameToken> = nodeKeys + descriptorKeys
//
//            val items = keys.map { token ->
//                val actualItem = node?.items?.get(token)
//                val actualDescriptor = descriptor?.children?.get(token.body)
//
//                if (actualItem is MetaNode) {
//                    FXMetaNode(token, this@FXMetaNode)
//                } else {
//                    FXMetaValue(token, this@FXMetaNode)
//                }
//            }
//
//            return items.filter(filter).asObservable()
//        }
//    }
//
//    init {
//        if (parent != null) {
//            parent.descriptorProperty.onChange { descriptorProperty.invalidate() }
//            parent.nodeProperty.onChange { nodeProperty.invalidate() }
//        }
//    }
//
}

//
//internal fun <M : MutableMeta> FXMeta<M>.remove(name: NameToken) {
//    node?.remove(name.asName())
//    children.invalidate()
//}
//
//private fun <M : MutableMeta> M.createEmptyNode(token: NameToken, append: Boolean): M {
//    return if (append && token.hasIndex()) {
//        val name = token.asName()
//        val index = (getIndexed(name).keys.mapNotNull { it?.toIntOrNull() }.maxOrNull() ?: -1) + 1
//        val newName = name.withIndex(index.toString())
//        set(newName, Meta.EMPTY)
//        get(newName).node
//    } else {
//        this.set(token.asName(), Meta.EMPTY)
//        //FIXME possible concurrency bug
//        get(token).node
//    }
//}
//
//internal fun <M : MutableMeta> FXMeta<out M>.getOrCreateNode(): M {
//    val node = node
//    return when {
//        node != null -> node
//        parent != null -> parent.getOrCreateNode().createEmptyNode(this.name, descriptor?.multiple == true).also {
//            parent.children.invalidate()
//        }
//        else -> kotlin.error("Orphan empty node is not allowed")
//    }
//
//}

internal fun <M : MutableMeta> FXMetaModel<M>.remove() {
    root.remove(nodeName)
}

//
//internal fun <M : MutableMeta> FXMeta<M>.addValue(key: String) {
//    val parent = getOrCreateNode()
//    if (descriptor?.multiple == true) {
//        parent.append(key, Null)
//    } else {
//        parent[key] = Null
//    }
//}
//
//internal fun <M : MutableMeta> FXMeta<M>.addNode(key: String) {
//    val parent = getOrCreateNode()
//    if (descriptor?.multiple == true) {
//        parent.append(key, Meta.EMPTY)
//    } else {
//        parent[key] = Meta.EMPTY
//    }
//}
//
internal fun <M : MutableMeta> FXMetaModel<M>.setValue(value: Value?) {
    root.setValue(nodeName, value)
}