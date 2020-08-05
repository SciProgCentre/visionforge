package hep.dataforge.vision.spatial

import hep.dataforge.meta.Meta
import hep.dataforge.meta.get
import hep.dataforge.meta.number
import kotlin.math.PI

object World {
    val ZERO = Point3D(0.0, 0.0, 0.0)
    val ONE = Point3D(1.0, 1.0, 1.0)
}

const val PI2: Float = 2 * PI.toFloat()

expect class Point2D(x: Number, y: Number) {
    var x: Double
    var y: Double
}

operator fun Point2D.component1() = x
operator fun Point2D.component2() = y

fun Point2D.toMeta() = Meta {
    VisualObject3D.X_KEY put x
    VisualObject3D.Y_KEY put y
}

fun Meta.point2D() = Point2D(this["x"].number ?: 0, this["y"].number ?: 0)

expect class Point3D(x: Number, y: Number, z: Number) {
    var x: Double
    var y: Double
    var z: Double
}

expect operator fun Point3D.plus(other: Point3D): Point3D

operator fun Point3D.component1() = x
operator fun Point3D.component2() = y
operator fun Point3D.component3() = z

fun Meta.point3D() = Point3D(this["x"].number ?: 0, this["y"].number ?: 0, this["y"].number ?: 0)

fun Point3D.toMeta() = Meta {
    VisualObject3D.X_KEY put x
    VisualObject3D.Y_KEY put y
    VisualObject3D.Z_KEY put z
}