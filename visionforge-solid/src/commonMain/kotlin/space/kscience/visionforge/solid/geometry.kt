package space.kscience.visionforge.solid

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaProvider
import space.kscience.dataforge.meta.float
import space.kscience.dataforge.meta.get
import space.kscience.visionforge.solid.Solid.Companion.X_KEY
import space.kscience.visionforge.solid.Solid.Companion.Y_KEY
import space.kscience.visionforge.solid.Solid.Companion.Z_KEY
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

public const val PI2: Float = 2 * PI.toFloat()

@Serializable
public data class Point2D(public var x: Float, public var y: Float)

public fun Point2D(x: Number, y: Number): Point2D = Point2D(x.toFloat(), y.toFloat())

public fun Point2D.toMeta(): Meta = Meta {
    X_KEY put x
    Y_KEY put y
}

internal fun Meta.point2D(): Point2D = Point2D(this["x"].float ?: 0f, this["y"].float ?: 0f)

@Serializable(Point3DSerializer::class)
public interface Point3D {
    public val x: Float
    public val y: Float
    public val z: Float

    public companion object {
        public val ZERO: Point3D = Point3D(0.0, 0.0, 0.0)
        public val ONE: Point3D = Point3D(1.0, 1.0, 1.0)
    }
}

@Serializable(Point3DSerializer::class)
public interface MutablePoint3D : Point3D {
    override var x: Float
    override var y: Float
    override var z: Float
}

@Serializable
private class Point3DImpl(override var x: Float, override var y: Float, override var z: Float) : MutablePoint3D

internal object Point3DSerializer : KSerializer<Point3D> {

    override val descriptor: SerialDescriptor = Point3DImpl.serializer().descriptor


    override fun deserialize(decoder: Decoder): MutablePoint3D = decoder.decodeSerializableValue(Point3DImpl.serializer())

    override fun serialize(encoder: Encoder, value: Point3D) {
        val impl: Point3DImpl = (value as? Point3DImpl) ?: Point3DImpl(value.x, value.y, value.z)
        encoder.encodeSerializableValue(Point3DImpl.serializer(), impl)
    }
}

public fun Point3D(x: Number, y: Number, z: Number): Point3D = Point3DImpl(x.toFloat(), y.toFloat(), z.toFloat())

public operator fun Point3D.plus(other: Point3D): Point3D = Point3D(
    this.x + other.x,
    this.y + other.y,
    this.z + other.z
)

public operator fun Point3D.minus(other: Point3D): Point3D = Point3D(
    this.x - other.x,
    this.y - other.y,
    this.z - other.z
)

public operator fun Point3D.unaryMinus(): Point3D = Point3D(
    -x,
    -y,
    -z
)

public infix fun Point3D.cross(other: Point3D): Point3D = Point3D(
    y * other.z - z * other.y,
    z * other.x - x * other.z,
    x * other.y - y * other.x
)

public fun MutablePoint3D.normalizeInPlace() {
    val norm = sqrt(x.pow(2) + y.pow(2) + z.pow(2))
    x /= norm
    y /= norm
    z /= norm
}

internal fun MetaProvider.point3D(default: Float = 0f) = object : Point3D {
    override val x: Float by float(default)
    override val y: Float by float(default)
    override val z: Float by float(default)
}

public fun Point3D.toMeta(): Meta = Meta {
    X_KEY put x
    Y_KEY put y
    Z_KEY put z
}


internal fun Meta.toVector(default: Float = 0f) = Point3D(
    this[Solid.X_KEY].float ?: default,
    this[Solid.Y_KEY].float ?: default,
    this[Solid.Z_KEY].float ?: default
)

internal fun Solid.updatePosition(meta: Meta?) {
    meta?.get(Solid.POSITION_KEY)?.toVector()?.let { position = it }
    meta?.get(Solid.ROTATION_KEY)?.toVector()?.let { rotation = it }
    meta?.get(Solid.SCALE_KEY)?.toVector(1f)?.let { scale = it }
}