package space.kscience.visionforge.solid

import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaBuilder
import space.kscience.dataforge.meta.float
import space.kscience.dataforge.meta.get
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

public const val PI2: Float = 2 * PI.toFloat()

@Serializable
public data class Point2D(public var x: Float, public var y: Float)

public fun Point2D(x: Number, y: Number): Point2D = Point2D(x.toFloat(), y.toFloat())

public fun Point2D.toMeta(): Meta = Meta {
    Solid.X_KEY put x
    Solid.Y_KEY put y
}

internal fun Meta.point2D(): Point2D = Point2D(this["x"].float ?: 0f, this["y"].float ?: 0f)

@Serializable
public data class Point3D(
    public var x: Float,
    public var y: Float,
    public var z: Float,
) {
    public companion object {
        public val ZERO: Point3D = Point3D(0.0, 0.0, 0.0)
        public val ONE: Point3D = Point3D(1.0, 1.0, 1.0)
    }
}

public fun Point3D(x: Number, y: Number, z: Number): Point3D = Point3D(x.toFloat(), y.toFloat(), z.toFloat())

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

public fun Point3D.normalizeInPlace(){
    val norm = sqrt(x.pow(2) + y.pow(2) + z.pow(2))
    x /= norm
    y /= norm
    z /= norm
}

internal fun Meta.point3D() = Point3D(this["x"].float ?: 0.0, this["y"].float ?: 0.0, this["y"].float ?: 0.0)

public fun Point3D.toMeta(): MetaBuilder = Meta {
    Solid.X_KEY put x
    Solid.Y_KEY put y
    Solid.Z_KEY put z
}