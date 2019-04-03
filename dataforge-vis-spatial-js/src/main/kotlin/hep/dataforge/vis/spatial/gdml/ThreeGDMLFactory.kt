package hep.dataforge.vis.spatial.gdml

import hep.dataforge.meta.Meta
import hep.dataforge.meta.buildMeta
import hep.dataforge.meta.toDynamic
import hep.dataforge.meta.values
import hep.dataforge.vis.DisplayLeaf
import hep.dataforge.vis.DisplayObject
import hep.dataforge.vis.int
import hep.dataforge.vis.spatial.MeshThreeFactory
import hep.dataforge.vis.spatial.jsroot.createGeometry
import hep.dataforge.vis.spatial.jsroot.createTubeBuffer
import info.laht.threekt.core.BufferGeometry


class GDMLShape(parent: DisplayObject?, meta: Meta, val shape: GDMLSolid) :
    DisplayLeaf(parent, "$TYPE.${shape.type}", meta) {

    var facesLimit by int(0)

    companion object {
        const val TYPE = "geometry.3d.gdml"
    }
}


object ThreeGDMLFactory : MeshThreeFactory<GDMLShape>(GDMLShape::class) {
    //TODO fix ineffective conversion
    private fun Meta?.toJsRoot() = this?.let {
        buildMeta {
            values().forEach { (name, value) ->
                name.toString().replaceFirst("p", "f") to value
            }
        }
    }

    override fun buildGeometry(obj: GDMLShape): BufferGeometry {
        return when (obj.shape) {
            is GDMLBox -> createTubeBuffer(
                obj.shape.config.toJsRoot()?.toDynamic(),
                obj.facesLimit
            )
            is GDMLTube -> createTubeBuffer(
                obj.shape.config.toJsRoot()?.toDynamic(),
                obj.facesLimit
            )
            is GDMLXtru -> {
                val meta = buildMeta {
                    "_typename" to "TGeoXtru"
                    val vertices = obj.shape.verteces
                    val zs = obj.shape.sections.sortedBy { it.zOrder!! }
                    "fNz" to zs.size
                    "fNvert" to vertices.size
                    "fX" to vertices.map { it.x }
                    "fY" to vertices.map { it.y }
                    "fX0" to zs.map { it.xOffsset }
                    "fY0" to zs.map { it.yOffset }
                    "fZ" to zs.map { it.zPosition!! }
                    "fScale" to zs.map { it.scalingFactor }
                }
                createGeometry(meta.toDynamic(), obj.facesLimit)
            }
            is GDMLUnion -> TODO()
            is GDMLSubtraction -> TODO()
            is GDMLIntersection -> TODO()
//            is GDMLUnion -> {
//                val meta = buildMeta {
//                           "fNode.fLeft" to obj.shape.first.toJsRoot()
//                    "fNode.fRight" to obj.shape.second.toJsRoot()
//                    "fNode._typename" to "TGeoUnion"
//                }
//                createGeometry(meta.toDynamic(), obj.facesLimit)
//            }

        }
    }
}