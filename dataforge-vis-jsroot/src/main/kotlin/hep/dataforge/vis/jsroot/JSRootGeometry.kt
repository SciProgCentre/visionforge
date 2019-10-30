package hep.dataforge.vis.jsroot

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.meta.buildMeta
import hep.dataforge.meta.toDynamic
import hep.dataforge.vis.common.*
import hep.dataforge.vis.spatial.three.MeshThreeFactory
import info.laht.threekt.core.BufferGeometry

class JSRootGeometry(parent: VisualObject?, meta: Meta) : DisplayLeaf(parent, meta) {

    var shape by node()

    var facesLimit by int(0)

    fun box(xSize: Number, ySize: Number, zSize: Number) = buildMeta {
        "_typename" put "TGeoBBox"
        "fDX" put xSize
        "fDY" put ySize
        "fDZ" put zSize
    }

    /**
     * Create a GDML union
     */
    operator fun Meta.plus(other: Meta) = buildMeta {
        "fNode.fLeft" put this
        "fNode.fRight" put other
        "fNode._typename" put "TGeoUnion"
    }

    /**
     * Create a GDML subtraction
     */
    operator fun Meta.minus(other: Meta)  = buildMeta {
        "fNode.fLeft" put this
        "fNode.fRight" put other
        "fNode._typename" put "TGeoSubtraction"
    }

    /**
     * Intersect two GDML geometries
     */
    infix fun Meta.intersect(other: Meta) = buildMeta {
        "fNode.fLeft" put this
        "fNode.fRight" put other
        "fNode._typename" put "TGeoIntersection"
    }

    companion object {
        const val TYPE = "geometry.spatial.jsRoot.geometry"
    }
}

fun VisualGroup.jsRootGeometry(meta: Meta = EmptyMeta, action: JSRootGeometry.() -> Unit = {}) =
    JSRootGeometry(this, meta).apply(action).also { add(it) }

//fun Meta.toDynamic(): dynamic {
//    fun MetaItem<*>.toDynamic(): dynamic = when (this) {
//        is MetaItem.ValueItem -> this.value.value.asDynamic()
//        is MetaItem.NodeItem -> this.node.toDynamic()
//    }
//
//    val res = js("{}")
//    this.items.entries.groupBy { it.key.body }.forEach { (key, value) ->
//        val list = value.map { it.value }
//        res[key] = when (list.size) {
//            1 -> list.first().toDynamic()
//            else -> list.map { it.toDynamic() }
//        }
//    }
//    return res
//}


object ThreeJSRootGeometryFactory : MeshThreeFactory<JSRootGeometry>(JSRootGeometry::class) {
    override fun buildGeometry(obj: JSRootGeometry): BufferGeometry {
        val shapeMeta = obj.shape?.toDynamic() ?: error("The shape not defined")
        return createGeometry(shapeMeta, obj.facesLimit)
    }
}