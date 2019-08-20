package hep.dataforge.vis.spatial

import hep.dataforge.names.NameToken
import hep.dataforge.names.toName
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor

@Serializable
private data class Point2DSerial(val x: Double, val y: Double)

@Serializable
private data class Point3DSerial(val x: Double, val y: Double, val z: Double)

@Serializer(Point3D::class)
object Point3DSerializer : KSerializer<Point3D> {
    private val serializer = Point3DSerial.serializer()
    override val descriptor: SerialDescriptor get() = serializer.descriptor

    override fun deserialize(decoder: Decoder): Point3D {
        return serializer.deserialize(decoder).let {
            Point3D(it.x, it.y, it.z)
        }
    }

    override fun serialize(encoder: Encoder, obj: Point3D) {
        serializer.serialize(encoder, Point3DSerial(obj.x, obj.y, obj.z))
    }
}

@Serializer(Point2D::class)
object Point2DSerializer : KSerializer<Point2D> {
    private val serializer = Point2DSerial.serializer()
    override val descriptor: SerialDescriptor get() = serializer.descriptor

    override fun deserialize(decoder: Decoder): Point2D {
        return serializer.deserialize(decoder).let {
            Point2D(it.x, it.y)
        }
    }

    override fun serialize(encoder: Encoder, obj: Point2D) {
        serializer.serialize(encoder, Point2DSerial(obj.x, obj.y))
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