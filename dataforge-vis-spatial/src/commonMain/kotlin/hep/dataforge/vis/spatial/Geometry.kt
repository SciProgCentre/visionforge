package hep.dataforge.vis.spatial

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaRepr
import hep.dataforge.meta.buildMeta


data class Point2D(val x: Number, val y: Number)

typealias Shape2D = List<Point2D>

data class Point3D(val x: Number, val y: Number, val z: Number): MetaRepr{
    override fun toMeta(): Meta = buildMeta {
        "x" to x
        "y" to y
        "z" to z
    }
}