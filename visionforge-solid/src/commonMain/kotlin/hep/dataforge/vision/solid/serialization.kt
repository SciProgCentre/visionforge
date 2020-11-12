package hep.dataforge.vision.solid

import hep.dataforge.names.NameToken
import hep.dataforge.vision.MutableVisionGroup
import hep.dataforge.vision.Vision
import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*


@OptIn(ExperimentalSerializationApi::class)
public object Point3DSerializer : KSerializer<Point3D> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("hep.dataforge.vis.spatial.Point3D") {
        element<Double>("x")
        element<Double>("y")
        element<Double>("z")
    }

    override fun deserialize(decoder: Decoder): Point3D {
        var x: Double? = null
        var y: Double? = null
        var z: Double? = null
        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    0 -> x = decodeNullableSerializableElement(descriptor, 0, Double.serializer().nullable) ?: 0.0
                    1 -> y = decodeNullableSerializableElement(descriptor, 1, Double.serializer().nullable) ?: 0.0
                    2 -> z = decodeNullableSerializableElement(descriptor, 2, Double.serializer().nullable) ?: 0.0
                    else -> throw SerializationException("Unknown index $i")
                }
            }
        }
        return Point3D(x ?: 0.0, y ?: 0.0, z ?: 0.0)
    }

    override fun serialize(encoder: Encoder, value: Point3D) {
        encoder.encodeStructure(descriptor) {
            if (value.x != 0.0) encodeDoubleElement(descriptor, 0, value.x)
            if (value.y != 0.0) encodeDoubleElement(descriptor, 1, value.y)
            if (value.z != 0.0) encodeDoubleElement(descriptor, 2, value.z)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
public object Point2DSerializer : KSerializer<Point2D> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("hep.dataforge.vis.spatial.Point2D") {
        element<Double>("x")
        element<Double>("y")
    }

    override fun deserialize(decoder: Decoder): Point2D {
        var x: Double? = null
        var y: Double? = null
        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    0 -> x = decodeNullableSerializableElement(descriptor, 0, Double.serializer().nullable) ?: 0.0
                    1 -> y = decodeNullableSerializableElement(descriptor, 1, Double.serializer().nullable) ?: 0.0
                    else -> throw SerializationException("Unknown index $i")
                }
            }
        }
        return Point2D(x ?: 0.0, y ?: 0.0)
    }

    override fun serialize(encoder: Encoder, value: Point2D) {
        encoder.encodeStructure(descriptor) {
            if (value.x != 0.0) encodeDoubleElement(descriptor, 0, value.x)
            if (value.y != 0.0) encodeDoubleElement(descriptor, 1, value.y)
        }
    }
}

internal object PrototypesSerializer : KSerializer<MutableVisionGroup> {

    private val mapSerializer: KSerializer<Map<NameToken, Vision>> =
        MapSerializer(
            NameToken.serializer(),
            Vision.serializer()
        )

    override val descriptor: SerialDescriptor get() = mapSerializer.descriptor

    override fun deserialize(decoder: Decoder): MutableVisionGroup {
        val map = mapSerializer.deserialize(decoder)
        return Prototypes(map as? MutableMap<NameToken, Vision> ?: LinkedHashMap(map))
    }

    override fun serialize(encoder: Encoder, value: MutableVisionGroup) {
        mapSerializer.serialize(encoder, value.children)
    }
}