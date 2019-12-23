package hep.dataforge.vis.spatial

actual data class Point2D(actual var x: Double, actual var y: Double){
    actual constructor(x: Number, y: Number): this(x.toDouble(),y.toDouble())
}

actual class Point3D(val point: org.fxyz3d.geometry.Point3D) {
    actual constructor(x: Number, y: Number, z: Number) : this(
        org.fxyz3d.geometry.Point3D(
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
        return this.point == (other as? hep.dataforge.vis.spatial.Point3D)?.point
    }

    override fun hashCode(): Int {
        return point.hashCode()
    }

    override fun toString(): String {
        return point.toString()
    }
}