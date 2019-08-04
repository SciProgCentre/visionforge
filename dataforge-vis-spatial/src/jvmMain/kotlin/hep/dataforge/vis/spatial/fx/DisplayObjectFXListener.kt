package hep.dataforge.vis.spatial.fx

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vis.common.VisualObject
import javafx.beans.binding.ObjectBinding
import tornadofx.*

/**
 * A caching binding collection for [VisualObject] properties
 */
class DisplayObjectFXListener(val obj: VisualObject) {
    private val binndings = HashMap<Name, ObjectBinding<MetaItem<*>?>>()

    init {
        obj.onPropertyChange(this) { name, _, _ ->
            binndings[name]?.invalidate()
        }
    }

    operator fun get(key: Name): ObjectBinding<MetaItem<*>?> {
        return binndings.getOrPut(key) {
            object : ObjectBinding<MetaItem<*>?>() {
                override fun computeValue(): MetaItem<*>? = obj.getProperty(key)
            }
        }
    }

    operator fun get(key: String) = get(key.toName())
}

fun ObjectBinding<MetaItem<*>?>.value() = this.objectBinding { it.value }
fun ObjectBinding<MetaItem<*>?>.string() = this.stringBinding { it.string }
fun ObjectBinding<MetaItem<*>?>.number() = this.objectBinding { it.number }
fun ObjectBinding<MetaItem<*>?>.double() = this.objectBinding { it.double }
fun ObjectBinding<MetaItem<*>?>.float() = this.objectBinding { it.number?.toFloat() }
fun ObjectBinding<MetaItem<*>?>.int() = this.objectBinding { it.int }
fun ObjectBinding<MetaItem<*>?>.long() = this.objectBinding { it.long }
fun ObjectBinding<MetaItem<*>?>.node() = this.objectBinding { it.node }

fun <T> ObjectBinding<MetaItem<*>?>.transform(transform: (MetaItem<*>) -> T) = this.objectBinding { it?.let(transform) }
