package hep.dataforge.vis.fx.meta

import hep.dataforge.descriptors.NodeDescriptor
import hep.dataforge.meta.*
import hep.dataforge.names.NameToken
import hep.dataforge.names.asName
import hep.dataforge.values.Value
import javafx.beans.binding.ListBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableStringValue
import javafx.collections.ObservableList
import tornadofx.*

sealed class FXMeta {
    abstract val name: NameToken
    abstract val parent: FXMetaNode<*>?
    abstract val descriptionProperty: ObservableStringValue

    abstract val hasValue: ObservableBooleanValue

    companion object {
        fun <M : MetaNode<M>> root(node: M, descriptor: NodeDescriptor? = null): FXMetaNode<M> =
            FXMetaNode(NameToken("root"), null, node, descriptor)

        fun root(node: Meta, descriptor: NodeDescriptor? = null): FXMetaNode<SealedMeta> =
            root(node.seal(), descriptor)
    }
}

class FXMetaNode<M : MetaNode<M>>(
    override val name: NameToken,
    override val parent: FXMetaNode<M>?,
    node: M? = null,
    descriptor: NodeDescriptor? = null
) : FXMeta() {

    /**
     * A descriptor that could be manually set to the node
     */
    val descriptorProperty = SimpleObjectProperty(descriptor)

    /**
     * Actual descriptor which holds value inferred from parrent
     */
    private val actualDescriptorProperty = objectBinding(descriptorProperty) {
        value ?: parent?.descriptor?.nodes?.get(this@FXMetaNode.name.body)
    }

    val descriptor: NodeDescriptor? by actualDescriptorProperty

    private val innerNodeProperty = SimpleObjectProperty(node)

    val nodeProperty: ObjectBinding<M?> = objectBinding(innerNodeProperty) {
        value ?: parent?.node?.get(this@FXMetaNode.name.asName()).node
    }

    val node: M? by nodeProperty

    override val descriptionProperty = descriptorProperty.stringBinding { it?.info ?: "" }

    override val hasValue: ObservableBooleanValue = nodeProperty.booleanBinding { it != null }

    val children = object : ListBinding<FXMeta>() {
        override fun computeValue(): ObservableList<FXMeta> {
            TODO()
        }
    }
}

class FXMetaValue(
    override val name: NameToken,
    override val parent: FXMetaNode<*>,
    value: Value? = null
) : FXMeta() {

    val descriptorProperty = parent.descriptorProperty.objectBinding {
        it?.values?.get(name.body)
    }

    /**
     * A descriptor that could be manually set to the node
     */
    val descriptor by descriptorProperty

    private val innerValueProperty = SimpleObjectProperty(value)

    val valueProperty = descriptorProperty.objectBinding { descriptor ->
        parent.node[name].value ?: descriptor?.default
    }

    override val hasValue: ObservableBooleanValue = valueProperty.booleanBinding { it != null }

    val value by valueProperty

    override val descriptionProperty = descriptorProperty.stringBinding { it?.info ?: "" }
}

fun <M : MutableMeta<M>> FXMetaNode<M>.remove(name: NameToken) {
    node?.remove(name.asName())
    children.invalidate()
}

fun FXMeta.remove() {
    (parent?.node as? MutableMeta<*>)?.remove(name.asName())
}

fun <M : MutableMeta<M>> FXMetaNode<M>.addValue(key: String){
    TODO()
}

fun <M : MutableMeta<M>> FXMetaNode<M>.addNode(key: String){
    TODO()
}

fun FXMetaValue.set(value: Value?){
    TODO()
}