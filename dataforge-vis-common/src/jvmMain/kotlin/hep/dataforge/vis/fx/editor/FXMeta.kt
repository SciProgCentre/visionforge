package hep.dataforge.vis.fx.editor

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.ItemDescriptor
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.meta.descriptors.ValueDescriptor
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.asName
import hep.dataforge.names.withIndex
import hep.dataforge.values.Null
import hep.dataforge.values.Value
import javafx.beans.binding.ListBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableStringValue
import javafx.collections.ObservableList
import tornadofx.*

/**
 * A display for meta and descriptor
 */
sealed class FXMeta<M : MetaNode<M>> : Comparable<FXMeta<*>> {
    abstract val name: NameToken
    abstract val parent: FXMetaNode<M>?
    abstract val descriptionProperty: ObservableStringValue
    abstract val descriptor: ItemDescriptor?

    abstract val hasValue: ObservableBooleanValue

    override fun compareTo(other: FXMeta<*>): Int {
        return if (this.hasValue.get() == other.hasValue.get()) {
            this.name.toString().compareTo(other.name.toString())
        } else {
            this.hasValue.get().compareTo(other.hasValue.get())
        }
    }

    companion object {
        fun <M : MetaNode<M>> root(
            node: M,
            descriptor: NodeDescriptor? = null,
            rootName: String = "root"
        ): FXMetaNode<M> =
            FXMetaNode(NameToken(rootName), null, node, descriptor)

        fun root(node: Meta, descriptor: NodeDescriptor? = null, rootName: String = "root"): FXMetaNode<SealedMeta> =
            root(node.seal(), descriptor, rootName)
    }
}

class FXMetaNode<M : MetaNode<M>>(
    override val name: NameToken,
    override val parent: FXMetaNode<M>?,
    nodeValue: M? = null,
    descriptorValue: NodeDescriptor? = null
) : FXMeta<M>() {

    /**
     * A descriptor that could be manually set to the node
     */
    private val innerDescriptorProperty = SimpleObjectProperty(descriptorValue)

    /**
     * Actual descriptor which holds value inferred from parrent
     */
    val descriptorProperty = objectBinding(innerDescriptorProperty) {
        value ?: parent?.descriptor?.nodes?.get(this@FXMetaNode.name.body)
    }

    override val descriptor: NodeDescriptor? by descriptorProperty

    private val innerNodeProperty = SimpleObjectProperty(nodeValue)

    val nodeProperty: ObjectBinding<M?> = objectBinding(innerNodeProperty) {
        value ?: parent?.node?.get(this@FXMetaNode.name).node
    }

    val node: M? by nodeProperty

    override val descriptionProperty = innerDescriptorProperty.stringBinding { it?.info ?: "" }

    override val hasValue: ObservableBooleanValue = nodeProperty.booleanBinding { it != null }

    private val filter: (FXMeta<M>) -> Boolean = { cfg ->
        !(cfg.descriptor?.attributes?.get(ConfigEditor.NO_CONFIGURATOR_TAG)?.boolean ?: false)
    }

    val children = object : ListBinding<FXMeta<M>>() {

        init {
            bind(nodeProperty, descriptorProperty)

            val listener: (Name, MetaItem<*>?, MetaItem<*>?) -> Unit = { name, _, _ ->
                if (name.length == 1) invalidate()
            }

            (node as? Config)?.onChange(this, listener)

            nodeProperty.addListener { _, oldValue, newValue ->
                if (newValue == null) {
                    (oldValue as? Config)?.removeListener(this)
                }

                if (newValue is Config) {
                    newValue.onChange(this, listener)
                }
            }
        }

        override fun computeValue(): ObservableList<FXMeta<M>> {
            val nodeKeys = node?.items?.keys?.toSet() ?: emptySet()
            val descriptorKeys = descriptor?.items?.keys?.map { NameToken(it) } ?: emptyList()
            val keys: Set<NameToken> = nodeKeys + descriptorKeys

            val items = keys.map { token ->
                val actualItem = node?.items?.get(token)
                val actualDescriptor = descriptor?.items?.get(token.body)

                if (actualItem is MetaItem.NodeItem || actualDescriptor is NodeDescriptor) {
                    FXMetaNode(token, this@FXMetaNode)
                } else {
                    FXMetaValue(token, this@FXMetaNode)
                }
            }

            return items.filter(filter).asObservable()
        }
    }

    init {
        if (parent != null) {
            parent.descriptorProperty.onChange { descriptorProperty.invalidate() }
            parent.nodeProperty.onChange { nodeProperty.invalidate() }
        }
    }
}

class FXMetaValue<M : MetaNode<M>>(
    override val name: NameToken,
    override val parent: FXMetaNode<M>
) : FXMeta<M>() {

    val descriptorProperty = parent.descriptorProperty.objectBinding {
        it?.values?.get(name.body)
    }

    /**
     * A descriptor that could be manually set to the node
     */
    override val descriptor: ValueDescriptor? by descriptorProperty

    //private val innerValueProperty = SimpleObjectProperty(value)

    val valueProperty = descriptorProperty.objectBinding { descriptor ->
        parent.node[name].value ?: descriptor?.default
    }

    override val hasValue: ObservableBooleanValue = parent.nodeProperty.booleanBinding { it[name] != null }

    val value by valueProperty

    override val descriptionProperty = descriptorProperty.stringBinding { it?.info ?: "" }
}

fun <M : MutableMeta<M>> FXMetaNode<M>.remove(name: NameToken) {
    node?.remove(name.asName())
    children.invalidate()
}

private fun <M : MutableMeta<M>> M.createEmptyNode(token: NameToken, append: Boolean): M {
    return if (append && token.index.isNotEmpty()) {
        val name = token.asName()
        val index = (getIndexed(name).keys.mapNotNull { it.toIntOrNull() }.max() ?: -1) + 1
        val newName = name.withIndex(index.toString())
        set(newName, EmptyMeta)
        get(newName).node!!
    } else {
        this.setNode(token.asName(), EmptyMeta)
        //FIXME possible concurrency bug
        get(token).node!!
    }
}

fun <M : MutableMeta<M>> FXMetaNode<out M>.getOrCreateNode(): M {
    val node = node
    return when {
        node != null -> node
        parent != null -> parent.getOrCreateNode().createEmptyNode(this.name, descriptor?.multiple == true).also {
            parent.children.invalidate()
        }
        else -> kotlin.error("Orphan empty node is not allowed")
    }

}

fun <M : MutableMeta<M>> FXMeta<M>.remove() {
    parent?.node?.remove(name.asName())
}

fun <M : MutableMeta<M>> FXMetaNode<M>.addValue(key: String) {
    val parent = getOrCreateNode()
    if (descriptor?.multiple == true) {
        parent.append(key, Null)
    } else {
        parent[key] = Null
    }
}

fun <M : MutableMeta<M>> FXMetaNode<M>.addNode(key: String) {
    val parent = getOrCreateNode()
    if (descriptor?.multiple == true) {
        parent.append(key, EmptyMeta)
    } else {
        parent[key] = EmptyMeta
    }
}

fun <M : MutableMeta<M>> FXMetaValue<M>.set(value: Value?) {
    if (descriptor?.multiple == true) {
        parent.getOrCreateNode().append(this.name.body, value)
    } else {
        parent.getOrCreateNode()[this.name] = value
    }
}