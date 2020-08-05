package hep.dataforge.vision.spatial

import hep.dataforge.meta.double
import hep.dataforge.names.NameToken
import hep.dataforge.vision.MutableVisualGroup
import hep.dataforge.vision.VisualGroup
import hep.dataforge.vision.VisualObject
import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer

inline fun <R> Decoder.decodeStructure(
    desc: SerialDescriptor,
    vararg typeParams: KSerializer<*> = emptyArray(),
    crossinline block: CompositeDecoder.() -> R
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
    override val descriptor: SerialDescriptor = SerialDescriptor("hep.dataforge.vis.spatial.Point3D") {
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

@Serializer(Point2D::class)
object Point2DSerializer : KSerializer<Point2D> {
    override val descriptor: SerialDescriptor = SerialDescriptor("hep.dataforge.vis.spatial.Point2D") {
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

@Serializer(MutableVisualGroup::class)
internal object PrototypesSerializer : KSerializer<MutableVisualGroup> {

    private val mapSerializer: KSerializer<Map<NameToken, VisualObject>> =
        MapSerializer(
            NameToken.serializer(),
            VisualObject.serializer()
        )

    override val descriptor: SerialDescriptor get() = mapSerializer.descriptor

    override fun deserialize(decoder: Decoder): MutableVisualGroup {
        val map = mapSerializer.deserialize(decoder)
        return Prototypes(map as? MutableMap<NameToken, VisualObject> ?: LinkedHashMap(map))
    }

    override fun serialize(encoder: Encoder, value: MutableVisualGroup) {
        mapSerializer.serialize(encoder, value.children)
    }
}

fun VisualObject.stringify(): String = Visual3D.json.stringify(VisualObject.serializer(), this)

fun VisualObject.Companion.parseJson(str: String) = Visual3D.json.parse(VisualObject.serializer(), str).also {
    if(it is VisualGroup){
        it.attachChildren()
    }
}