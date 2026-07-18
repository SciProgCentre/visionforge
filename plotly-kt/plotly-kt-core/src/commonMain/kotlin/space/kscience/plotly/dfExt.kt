package space.kscience.plotly

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

//extensions for DataForge


/**
 * List of values delegate
 */
internal fun MutableMetaProvider.listOfValues(
    key: Name? = null,
): ReadWriteProperty<Any?, List<Value>> = object : ReadWriteProperty<Any?, List<Value>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): List<Value> {
        val name = key ?: Name.of(property.name)
        return getValue(name)?.list ?: emptyList()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: List<Value>) {
        val name = key ?: Name.of(property.name)
        setValue(name, value.asValue())
    }
}

/**
 * A safe [Double] range
 */
internal fun MutableMetaProvider.doubleInRange(
    range: ClosedFloatingPointRange<Double>,
    defaultValue: Double = Double.NaN,
    key: Name? = null,
): ReadWriteProperty<Any?, Double> = object : ReadWriteProperty<Any?, Double> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Double {
        val name = key ?: Name.of(property.name)
        return getValue(name)?.double ?: defaultValue
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
        val name = key ?: Name.of(property.name)
        if (value in range) {
            setValue(name, value.asValue())
        } else {
            error("$value not in range $range")
        }
    }
}

/**
 * A safe [Double] ray
 */
internal fun MutableMetaProvider.doubleGreaterThan(
    minValue: Double,
    key: Name? = null,
): ReadWriteProperty<Any?, Double> = object : ReadWriteProperty<Any?, Double> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Double {
        val name = key ?: Name.of(property.name)
        return getValue(name)?.double ?: Double.NaN
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
        val name = key ?: Name.of(property.name)
        if (value >= minValue) {
            setValue(name, value.asValue())
        } else {
            error("$value less than $minValue")
        }
    }
}


/**
 * A safe [Int] ray
 */
internal fun MutableMetaProvider.intGreaterThan(
    minValue: Int,
    key: Name? = null,
): ReadWriteProperty<Any?, Int> = object : ReadWriteProperty<Any?, Int> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        val name = key ?: Name.of(property.name)
        return getValue(name)?.int ?: minValue
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        val name = key ?: Name.of(property.name)
        if (value >= minValue) {
            setValue(name, value.asValue())
        } else {
            error("$value less than $minValue")
        }
    }
}

/**
 * A safe [Int] range
 */
internal fun MutableMetaProvider.intInRange(
    range: ClosedRange<Int>,
    key: Name? = null,
): ReadWriteProperty<Any?, Int> = object : ReadWriteProperty<Any?, Int> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        val name = key ?: Name.of(property.name)
        return getValue(name)?.int ?: 0
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        val name = key ?: Name.of(property.name)
        if (value in range) {
            setValue(name, value.asValue())
        } else {
            error("$value not in range $range")
        }
    }
}

/**
 * A safe [Number] ray
 */
public fun MutableMetaProvider.numberGreaterThan(
    minValue: Number,
    default: Number = minValue,
    key: Name? = null,
): ReadWriteProperty<Any?, Number> = object : ReadWriteProperty<Any?, Number> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Number {
        val name = key ?: Name.of(property.name)
        return getValue(name)?.number ?: default
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Number) {
        val name = key ?: Name.of(property.name)
        if (value.toDouble() >= minValue.toDouble()) {
            setValue(name, value.asValue())
        } else {
            error("$value less than $minValue")
        }
    }
}

/**
 * A safe [Number] range
 */
public fun MutableMetaProvider.numberInRange(
    range: ClosedRange<Double>,
    key: Name? = null,
): ReadWriteProperty<Any?, Number> = object : ReadWriteProperty<Any?, Number> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Number {
        val name = key ?: Name.of(property.name)
        return getValue(name)?.int ?: 0
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Number) {
        val name = key ?: Name.of(property.name)
        if (value.toDouble() in range) {
            setValue(name, value.asValue())
        } else {
            error("$value not in range $range")
        }
    }
}

internal fun MutableMetaProvider.duration(
    default: Duration? = null,
    key: Name? = null,
): ReadWriteProperty<Any?, Duration?> = object : ReadWriteProperty<Any?, Duration?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Duration? {
        val name = key ?: Name.of(property.name)
        val value = getValue(name)
        val units = getValue(name + "unit")?.enum() ?: DurationUnit.MILLISECONDS
        return value?.long?.toDuration(units) ?: default
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Duration?) {
        val name = key ?: Name.of(property.name)
        if (value == null) {
            remove(name)
        } else {
            setValue(name, value.toDouble(DurationUnit.MILLISECONDS).asValue())
            setValue(name + "unit", DurationUnit.MILLISECONDS.name.asValue())
        }
    }
}

/**
 * Append the observable note to same-name-siblings and observe its changes.
 */
@OptIn(DFExperimental::class)
internal fun ObservableMutableMeta.appendAndAttach(key: String, meta: ObservableMutableMeta) {
    val name = Name.parse(key)
    require(!name.isEmpty()) { "Name could not be empty for append operation" }
    val newIndex = name.lastOrNull()!!.index
    if (newIndex != null) {
        attach(name, meta)
    } else {
        val index = (getIndexed(name).keys.mapNotNull { it?.toIntOrNull() }.maxOrNull() ?: -1) + 1
        attach(name.withIndex(index.toString()), meta)
    }
}