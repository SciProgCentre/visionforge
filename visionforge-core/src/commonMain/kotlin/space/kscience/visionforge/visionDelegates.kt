package space.kscience.visionforge

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.transformations.MetaConverter
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.values.Value
import space.kscience.dataforge.values.number
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public fun Vision.propertyNode(
    name: Name? = null,
    inherit: Boolean = false,
    includeStyles: Boolean = true,
    includeDefaults: Boolean = true,
): ReadWriteProperty<Any?, Meta?> = object : ReadWriteProperty<Any?, Meta?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Meta? =
        getProperty(name ?: Name.parse(property.name), inherit, includeStyles, includeDefaults)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Meta?) {
        setPropertyNode(name ?: Name.parse(property.name), value)
    }
}

public fun <T> Vision.propertyNode(
    converter: MetaConverter<T>,
    name: Name? = null,
    inherit: Boolean = false,
    includeStyles: Boolean = true,
    includeDefaults: Boolean = true,
): ReadWriteProperty<Any?, T?> = object : ReadWriteProperty<Any?, T?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? = getProperty(
        name ?: Name.parse(property.name),
        inherit,
        includeStyles,
        includeDefaults
    )?.let(converter::metaToObject)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        setPropertyNode(name ?: Name.parse(property.name), value?.let(converter::objectToMeta))
    }
}

public fun Vision.propertyValue(
    name: Name? = null,
    inherit: Boolean = false,
    includeStyles: Boolean = true,
    includeDefaults: Boolean = true,
): ReadWriteProperty<Any?, Value?> = object : ReadWriteProperty<Any?, Value?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Value? =
        getProperty(name ?: Name.parse(property.name), inherit, includeStyles, includeDefaults)?.value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Value?) {
        setPropertyValue(name ?: Name.parse(property.name), value)
    }
}

public fun <T> Vision.propertyValue(
    name: Name? = null,
    inherit: Boolean = false,
    includeStyles: Boolean = true,
    includeDefaults: Boolean = true,
    setter: (T) -> Value? = { it?.let(Value::of) },
    getter: (Value?) -> T,
): ReadWriteProperty<Any?, T> = object : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = getProperty(
        name ?: Name.parse(property.name),
        inherit,
        includeStyles,
        includeDefaults
    )?.value.let(getter)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        setPropertyValue(name ?: Name.parse(property.name), value?.let(setter))
    }
}

public fun Vision.numberProperty(
    name: Name? = null,
    inherit: Boolean = false,
    includeStyles: Boolean = true,
    includeDefaults: Boolean = true
): ReadWriteProperty<Any?, Number?> = propertyValue(name, inherit, includeStyles, includeDefaults) { it?.number }

public fun Vision.numberProperty(
    name: Name? = null,
    inherit: Boolean = false,
    includeStyles: Boolean = true,
    includeDefaults: Boolean = true,
    default: () -> Number
): ReadWriteProperty<Any?, Number> = propertyValue(name, inherit, includeStyles, includeDefaults) {
    it?.number ?: default()
}