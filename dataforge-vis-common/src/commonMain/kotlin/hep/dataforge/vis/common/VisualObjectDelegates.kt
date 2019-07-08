package hep.dataforge.vis.common

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.values.Value
import kotlin.jvm.JvmName
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A delegate for display object properties
 */
class DisplayObjectDelegate(
    val key: Name?,
    val default: MetaItem<*>?,
    val inherited: Boolean
) : ReadWriteProperty<VisualObject, MetaItem<*>?> {
    override fun getValue(thisRef: VisualObject, property: KProperty<*>): MetaItem<*>? {
        val name = key ?: property.name.toName()
        return if (inherited) {
            thisRef.getProperty(name)
        } else {
            thisRef.properties[name]
        } ?: default
    }

    override fun setValue(thisRef: VisualObject, property: KProperty<*>, value: MetaItem<*>?) {
        val name = key ?: property.name.toName()
        thisRef.properties[name] = value
    }
}

class DisplayObjectDelegateWrapper<T>(
    val key: Name?,
    val default: T,
    val inherited: Boolean,
    val write: Config.(name: Name, value: T) -> Unit = { name, value -> set(name, value) },
    val read: (MetaItem<*>?) -> T?
) : ReadWriteProperty<VisualObject, T> {
    override fun getValue(thisRef: VisualObject, property: KProperty<*>): T {
        val name = key ?: property.name.toName()
        return if (inherited) {
            read(thisRef.getProperty(name))
        } else {
            read(thisRef.properties[name])
        } ?: default
    }

    override fun setValue(thisRef: VisualObject, property: KProperty<*>, value: T) {
        val name = key ?: property.name.toName()
        thisRef.properties[name] = value
    }
}


fun VisualObject.value(default: Value? = null, key: String? = null, inherited: Boolean = true) =
    DisplayObjectDelegateWrapper(key?.toName(), default, inherited) { it.value }

fun VisualObject.string(default: String? = null, key: String? = null, inherited: Boolean = true) =
    DisplayObjectDelegateWrapper(key?.toName(), default, inherited) { it.string }

fun VisualObject.boolean(default: Boolean? = null, key: String? = null, inherited: Boolean = true) =
    DisplayObjectDelegateWrapper(key?.toName(), default, inherited) { it.boolean }

fun VisualObject.number(default: Number? = null, key: String? = null, inherited: Boolean = true) =
    DisplayObjectDelegateWrapper(key?.toName(), default, inherited) { it.number }

fun VisualObject.double(default: Double? = null, key: String? = null, inherited: Boolean = true) =
    DisplayObjectDelegateWrapper(key?.toName(), default, inherited) { it.double }

fun VisualObject.int(default: Int? = null, key: String? = null, inherited: Boolean = true) =
    DisplayObjectDelegateWrapper(key?.toName(), default, inherited) { it.int }


fun VisualObject.node(key: String? = null, inherited: Boolean = true) =
    DisplayObjectDelegateWrapper(key?.toName(), null, inherited) { it.node }

fun VisualObject.item(key: String? = null, inherited: Boolean = true) =
    DisplayObjectDelegateWrapper(key?.toName(), null, inherited) { it }

//fun <T : Configurable> Configurable.spec(spec: Specification<T>, key: String? = null) = ChildConfigDelegate<T>(key) { spec.wrap(this) }

@JvmName("safeString")
fun VisualObject.string(default: String, key: String? = null, inherited: Boolean = true) =
    DisplayObjectDelegateWrapper(key?.toName(), default, inherited) { it.string }

@JvmName("safeBoolean")
fun VisualObject.boolean(default: Boolean, key: String? = null, inherited: Boolean = true) =
    DisplayObjectDelegateWrapper(key?.toName(), default, inherited) { it.boolean }

@JvmName("safeNumber")
fun VisualObject.number(default: Number, key: String? = null, inherited: Boolean = true) =
    DisplayObjectDelegateWrapper(key?.toName(), default, inherited) { it.number }

@JvmName("safeDouble")
fun VisualObject.double(default: Double, key: String? = null, inherited: Boolean = true) =
    DisplayObjectDelegateWrapper(key?.toName(), default, inherited) { it.double }

@JvmName("safeInt")
fun VisualObject.int(default: Int, key: String? = null, inherited: Boolean = true) =
    DisplayObjectDelegateWrapper(key?.toName(), default, inherited) { it.int }

inline fun <reified E : Enum<E>> VisualObject.enum(default: E, key: String? = null, inherited: Boolean = true) =
    DisplayObjectDelegateWrapper(key?.toName(), default, inherited) { item -> item.string?.let { enumValueOf<E>(it) } }

//merge properties

fun <T> VisualObject.merge(
    key: String? = null,
    transformer: (Sequence<MetaItem<*>>) -> T
): ReadOnlyProperty<VisualObject, T> {
    return object : ReadOnlyProperty<VisualObject, T> {
        override fun getValue(thisRef: VisualObject, property: KProperty<*>): T {
            val name = key?.toName() ?: property.name.toName()
            val sequence = sequence<MetaItem<*>> {
                var thisObj: VisualObject? = thisRef
                while (thisObj != null) {
                    thisObj.properties[name]?.let { yield(it) }
                    thisObj = thisObj.parent
                }
            }
            return transformer(sequence)
        }
    }
}