package space.kscience.visionforge

import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.descriptors.NodeDescriptor
import space.kscience.dataforge.meta.descriptors.NodeDescriptorBuilder
import space.kscience.dataforge.meta.descriptors.ValueDescriptorBuilder
import space.kscience.dataforge.meta.toConfig
import space.kscience.dataforge.values.ValueType
import kotlin.reflect.KProperty1
import kotlin.reflect.typeOf

/**
 * TODO to be moved into the core
 */
public inline fun <S : Scheme, reified T> NodeDescriptorBuilder.value(
    property: KProperty1<S, T>,
    noinline block: ValueDescriptorBuilder.() -> Unit = {},
) {
    when (typeOf<T>()) {
        typeOf<Number>(), typeOf<Int>(), typeOf<Double>(), typeOf<Short>(), typeOf<Long>(), typeOf<Float>() ->
            value(property.name) {
                type(ValueType.NUMBER)
                block()
            }
        typeOf<Boolean>() -> value(property.name) {
            type(ValueType.BOOLEAN)
            block()
        }
        typeOf<List<Number>>(), typeOf<List<Int>>(), typeOf<List<Double>>(), typeOf<List<Short>>(), typeOf<List<Long>>(), typeOf<List<Float>>(),
        typeOf<IntArray>(), typeOf<DoubleArray>(), typeOf<ShortArray>(), typeOf<LongArray>(), typeOf<FloatArray>(),
        -> value(property.name) {
            type(ValueType.NUMBER)
            multiple = true
            block()
        }
        typeOf<String>() -> value(property.name) {
            type(ValueType.STRING)
            block()
        }
        typeOf<List<String>>(), typeOf<Array<String>>() -> value(property.name) {
            type(ValueType.STRING)
            multiple = true
            block()
        }
        else -> value(property.name, block)
    }
}

public fun NodeDescriptor.copy(block: NodeDescriptorBuilder.() -> Unit = {}): NodeDescriptor {
    return NodeDescriptorBuilder(toMeta().toConfig()).apply(block)
}

public inline fun <S : Scheme, reified T : Scheme> NodeDescriptorBuilder.scheme(
    property: KProperty1<S, T>,
    spec: SchemeSpec<T>,
    noinline block: NodeDescriptorBuilder.() -> Unit = {},
) {
    spec.descriptor?.let { descriptor ->
        item(property.name, descriptor.copy(block))
    }
}