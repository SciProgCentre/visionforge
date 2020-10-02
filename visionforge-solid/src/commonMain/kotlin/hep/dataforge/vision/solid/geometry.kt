package hep.dataforge.vision.solid

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.get
import hep.dataforge.meta.number
import kotlin.math.PI

public object World {
    public val ZERO: Point3D = Point3D(0.0, 0.0, 0.0)
    public val ONE: Point3D = Point3D(1.0, 1.0, 1.0)
}

public const val PI2: Float = 2 * PI.toFloat()

public expect class Point2D(x: Number, y: Number) {
    public var x: Double
    public var y: Double
}

public operator fun Point2D.component1(): Double = x
public operator fun Point2D.component2(): Double = y

public fun Point2D.toMeta(): Meta = Meta {
    Solid.X_KEY put x
    Solid.Y_KEY put y
}

internal fun Meta.point2D(): Point2D = Point2D(this["x"].number ?: 0, this["y"].number ?: 0)

public expect class Point3D(x: Number, y: Number, z: Number) {
    public var x: Double
    public var y: Double
    public var z: Double
}

public expect operator fun Point3D.plus(other: Point3D): Point3D

public operator fun Point3D.component1(): Double = x
public operator fun Point3D.component2(): Double = y
public operator fun Point3D.component3(): Double = z

internal fun Meta.point3D() = Point3D(this["x"].number ?: 0, this["y"].number ?: 0, this["y"].number ?: 0)

public fun Point3D.toMeta(): MetaBuilder = Meta {
    Solid.X_KEY put x
    Solid.Y_KEY put y
    Solid.Z_KEY put z
}