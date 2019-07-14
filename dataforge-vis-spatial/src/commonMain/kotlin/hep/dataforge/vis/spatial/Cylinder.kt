package hep.dataforge.vis.spatial

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.vis.common.DisplayLeaf
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.number
import kotlin.math.PI

/**
 * A cylinder or cut cone segment
 */
class Cylinder(parent: VisualObject?, meta: Meta) : DisplayLeaf(parent, meta) {
    var radius by number()
    var upperRadius by number(default = radius)
    var height by number()
    var startAngle by number(0.0)
    var angle by number(2* PI)
}

fun VisualGroup.cylinder(r: Number, height: Number, meta: Meta = EmptyMeta, block: Cylinder.()->Unit = {}):Cylinder{
    val cylinder = Cylinder(this,meta)
    cylinder.radius = r
    cylinder.height = height
    cylinder.apply(block)
    return cylinder
}