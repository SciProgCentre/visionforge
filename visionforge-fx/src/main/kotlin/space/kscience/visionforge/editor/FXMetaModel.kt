package space.kscience.visionforge.editor

import javafx.beans.binding.Binding
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.ListBinding
import javafx.beans.binding.ObjectBinding
import javafx.collections.ObservableList
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.ObservableMeta
import space.kscience.dataforge.meta.boolean
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.meta.get
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

    private val existingNode = object: ObjectBinding<Meta?>() {
        override fun computeValue(): Meta? =root[nodeName]
    }

    public val children: ListBinding<FXMetaModel<M>> = object : ListBinding<FXMetaModel<M>>() {
        override fun computeValue(): ObservableList<FXMetaModel<M>> {
            val nodeKeys = existingNode.get()?.items?.keys?: emptySet()
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
                    if (nodeName.length == changed.length) existingNode.invalidate()
                    else if (changed.length == nodeName.length + 1) children.invalidate()
                }
            }
        }
    }

    public val descriptor: MetaDescriptor? = rootDescriptor?.get(nodeName)

    public val existsProperty: BooleanBinding = existingNode.isNotNull

    public val exists: Boolean by existsProperty

    public val valueProperty: Binding<Value?> = existingNode.objectBinding {
        existingNode.get()?.value ?: descriptor?.defaultValue
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
}