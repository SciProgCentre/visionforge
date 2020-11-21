package hep.dataforge.vision.solid

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.double
import hep.dataforge.meta.get
import kotlinx.serialization.Serializable
import kotlin.math.PI

public const val PI2: Float = 2 * PI.toFloat()

@Serializable
public data class Point2D(public var x: Double, public var y: Double){
    public constructor(x: Number, y: Number) : this(x.toDouble(), y.toDouble())
}


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
    public constructor(x: Number, y: Number, z: Number) : this(x.toDouble(), y.toDouble(), z.toDouble())

    public companion object{
        public val ZERO: Point3D = Point3D(0.0, 0.0, 0.0)
        public val ONE: Point3D = Point3D(1.0, 1.0, 1.0)
    }
}

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