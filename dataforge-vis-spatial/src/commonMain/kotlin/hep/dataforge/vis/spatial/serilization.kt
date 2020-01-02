package hep.dataforge.vis.spatial

import hep.dataforge.io.serialization.descriptor
import hep.dataforge.names.NameToken
import hep.dataforge.names.toName
import kotlinx.serialization.*
import kotlinx.serialization.internal.DoubleSerializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.internal.nullable

inline fun <R> Decoder.decodeStructure(
    desc: SerialDescriptor,
    vararg typeParams: KSerializer<*> = emptyArray(),
    crossinline block:  CompositeDecoder.() -> R
): R {
    val decoder = beginStructure(desc, *typeParams)
    val res = decoder.block()
    decoder.endStructure(desc)
    return res
}

inline fun Encoder.encodeStructure(
    desc: SerialDescriptor,
    vararg typeParams: KSerializer<*> = emptyArray(),
    block: CompositeEncoder.() -> Unit
) {
    val encoder = beginStructure(desc, *typeParams)
    encoder.block()
    encoder.endStructure(desc)
}

@Serializer(Point3D::class)
object Point3DSerializer : KSerializer<Point3D> {
    override val descriptor: SerialDescriptor = descriptor("hep.dataforge.vis.spatial.Point3D") {
        double("x", true)
        double("y", true)
        double("z", true)
    }

    override fun deserialize(decoder: Decoder): Point3D {
        var x: Double? = null
        var y: Double? = null
        var z: Double? = null
        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> x = decodeNullableSerializableElement(descriptor, 0, DoubleSerializer.nullable) ?: 0.0
                    1 -> y = decodeNullableSerializableElement(descriptor, 0, DoubleSerializer.nullable) ?: 0.0
                    2 -> z = decodeNullableSerializableElement(descriptor, 0, DoubleSerializer.nullable) ?: 0.0
                    else -> throw SerializationException("Unknown index $i")
                }
            }
        }
        return Point3D(x?:0.0, y?:0.0, z?:0.0)
    }

    override fun serialize(encoder: Encoder, obj: Point3D) {
        encoder.encodeStructure(descriptor) {
            if (obj.x != 0.0) encodeDoubleElement(descriptor, 0, obj.x)
            if (obj.y != 0.0) encodeDoubleElement(descriptor, 1, obj.y)
            if (obj.z != 0.0) encodeDoubleElement(descriptor, 2, obj.z)
        }
    }
}

@Serializer(Point2D::class)
object Point2DSerializer : KSerializer<Point2D> {
    override val descriptor: SerialDescriptor = descriptor("hep.dataforge.vis.spatial.Point2D") {
        double("x", true)
        double("y", true)
    }

    override fun deserialize(decoder: Decoder): Point2D {
        var x: Double? = null
        var y: Double? = null
        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> x = decodeNullableSerializableElement(descriptor, 0, DoubleSerializer.nullable) ?: 0.0
                    1 -> y = decodeNullableSerializableElement(descriptor, 0, DoubleSerializer.nullable) ?: 0.0
                    else -> throw SerializationException("Unknown index $i")
                }
            }
        }
        return Point2D(x?:0.0, y?:0.0)
    }

    override fun serialize(encoder: Encoder, obj: Point2D) {
        encoder.encodeStructure(descriptor) {
            if (obj.x != 0.0) encodeDoubleElement(descriptor, 0, obj.x)
            if (obj.y != 0.0) encodeDoubleElement(descriptor, 1, obj.y)
        }
    }
}

@Serializer(NameToken::class)
object NameTokenSerializer : KSerializer<NameToken> {
    override val descriptor: SerialDescriptor = StringDescriptor.withName("NameToken")

    override fun deserialize(decoder: Decoder): NameToken {
        return decoder.decodeString().toName().first()!!
    }

    override fun serialize(encoder: Encoder, obj: NameToken) {
        encoder.encodeString(obj.toString())
    }
}