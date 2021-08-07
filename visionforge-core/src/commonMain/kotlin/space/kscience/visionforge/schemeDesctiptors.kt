package space.kscience.visionforge

import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.descriptors.Described
import space.kscience.dataforge.meta.descriptors.MetaDescriptorBuilder
import space.kscience.dataforge.meta.descriptors.item
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.values.ValueType
import kotlin.reflect.KProperty1
import kotlin.reflect.typeOf

/**
 * TODO to be moved into the core
 */
public inline fun <S : Scheme, reified T> MetaDescriptorBuilder.value(
    property: KProperty1<S, T>,
    noinline block: MetaDescriptorBuilder.() -> Unit = {},
) {
    when (typeOf<T>()) {
        typeOf<Number>(), typeOf<Int>(), typeOf<Double>(), typeOf<Short>(), typeOf<Long>(), typeOf<Float>() ->
            value(property.name, ValueType.NUMBER) {
                block()
            }
        typeOf<Number?>(), typeOf<Int?>(), typeOf<Double?>(), typeOf<Short?>(), typeOf<Long?>(), typeOf<Float?>() ->
            value(property.name, ValueType.NUMBER) {
                block()
            }
        typeOf<Boolean>() -> value(property.name, ValueType.BOOLEAN) {
            block()
        }
        typeOf<List<Number>>(), typeOf<List<Int>>(), typeOf<List<Double>>(), typeOf<List<Short>>(), typeOf<List<Long>>(), typeOf<List<Float>>(),
        typeOf<IntArray>(), typeOf<DoubleArray>(), typeOf<ShortArray>(), typeOf<LongArray>(), typeOf<FloatArray>(),
        -> value(property.name, ValueType.NUMBER) {
            multiple = true
            block()
        }
        typeOf<String>() -> value(property.name, ValueType.STRING) {
            block()
        }
        typeOf<List<String>>(), typeOf<Array<String>>() -> value(property.name, ValueType.STRING) {
            multiple = true
            block()
        }
        else -> item(property.name, block)
    }
}

public fun MetaDescriptorBuilder.item(
    key: String,
    described: Described,
    block: MetaDescriptorBuilder.() -> Unit = {},
) {
    described.descriptor?.let {
        item(Name.parse(key), it, block)
    }
}

public inline fun <S : Scheme, reified T : Scheme> MetaDescriptorBuilder.scheme(
    property: KProperty1<S, T>,
    spec: SchemeSpec<T>,
    noinline block: MetaDescriptorBuilder.() -> Unit = {},
) {
    item(property.name, spec, block)
}
