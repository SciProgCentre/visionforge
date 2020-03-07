package hep.dataforge.vis.spatial
import org.fxyz3d.geometry.Point3D as FXPoint3D

actual data class Point2D(actual var x: Double, actual var y: Double) {
    actual constructor(x: Number, y: Number) : this(x.toDouble(), y.toDouble())
}

actual class Point3D(val point: FXPoint3D) {
    actual constructor(x: Number, y: Number, z: Number) : this(
        FXPoint3D(
            x.toFloat(),
            y.toFloat(),
            z.toFloat()
        )
    )

    actual var x: Double
        inline get() = point.x.toDouble()
        inline set(value) {
            point.x = value.toFloat()
        }

    actual var y: Double
        inline get() = point.y.toDouble()
        inline set(value) {
            point.y = value.toFloat()
        }

    actual var z: Double
        inline get() = point.z.toDouble()
        inline set(value) {
            point.z = value.toFloat()
        }

    override fun equals(other: Any?): Boolean {
        return this.point == (other as? Point3D)?.point
    }

    override fun hashCode(): Int {
        return point.hashCode()
    }

    override fun toString(): String {
        return point.toString()
    }
}

actual operator fun Point3D.plus(other: Point3D): Point3D {
    return Point3D(point.add(other.point))
}