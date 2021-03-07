package space.kscience.visionforge.solid

import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaBuilder
import space.kscience.dataforge.meta.double
import space.kscience.dataforge.meta.get
import kotlin.math.PI

public const val PI2: Float = 2 * PI.toFloat()

@Serializable
public data class Point2D(public var x: Double, public var y: Double)

public fun Point2D(x: Number, y: Number): Point2D = Point2D(x.toDouble(), y.toDouble())

public fun Point2D.toMeta(): Meta = Meta {
    Solid.X_KEY put x
    Solid.Y_KEY put y
}

internal fun Meta.point2D(): Point2D = Point2D(this["x"].double ?: 0.0, this["y"].double ?: 0.0)

@Serializable
public data class Point3D(
    public var x: Double,
    public var y: Double,
    public var z: Double,
) {
    public companion object {
        public val ZERO: Point3D = Point3D(0.0, 0.0, 0.0)
        public val ONE: Point3D = Point3D(1.0, 1.0, 1.0)
    }
}

public fun Point3D(x: Number, y: Number, z: Number): Point3D = Point3D(x.toDouble(), y.toDouble(), z.toDouble())

public operator fun Point3D.plus(other: Point3D): Point3D = Point3D(
    this.x + other.x,
    this.y + other.y,
    this.z + other.z
)

internal fun Meta.point3D() = Point3D(this["x"].double ?: 0.0, this["y"].double ?: 0.0, this["y"].double ?: 0.0)

public fun Point3D.toMeta(): MetaBuilder = Meta {
    Solid.X_KEY put x
    Solid.Y_KEY put y
    Solid.Z_KEY put z
}