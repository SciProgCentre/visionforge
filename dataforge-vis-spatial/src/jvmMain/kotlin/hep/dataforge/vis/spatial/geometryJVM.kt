package hep.dataforge.vis.spatial

actual class Point2D actual constructor(x: Number, y: Number) {
    actual var x = x.toDouble()
    actual var y = y.toDouble()
}

actual class Point3D actual constructor(x: Number, y: Number, z: Number) {
    actual var x = x.toDouble()
    actual var y = y.toDouble()
    actual var z = z.toDouble()
}