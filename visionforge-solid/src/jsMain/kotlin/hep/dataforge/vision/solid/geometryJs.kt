package hep.dataforge.vision.solid


import info.laht.threekt.math.Vector2
import info.laht.threekt.math.Vector3
import info.laht.threekt.math.plus

actual typealias Point2D = Vector2

actual typealias Point3D = Vector3

actual operator fun Point3D.plus(other: Point3D): Point3D {
    return this.plus(other)
}