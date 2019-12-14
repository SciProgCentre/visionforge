package hep.dataforge.vis.spatial

actual class Point2D actual constructor(x: Number, y: Number) {
    actual var x = x.toDouble()
    actual var y = y.toDouble()
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
}