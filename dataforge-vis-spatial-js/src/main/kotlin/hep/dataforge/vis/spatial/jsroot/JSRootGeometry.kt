package hep.dataforge.vis.spatial.jsroot

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.meta.buildMeta
import hep.dataforge.meta.toDynamic
import hep.dataforge.vis.common.*
import hep.dataforge.vis.spatial.three.MeshThreeFactory
import info.laht.threekt.core.BufferGeometry

class JSRootGeometry(parent: DisplayObject?, meta: Meta) : DisplayLeaf(parent, meta) {

    var shape by node()

    var facesLimit by int(0)

    fun box(xSize: Number, ySize: Number, zSize: Number) = buildMeta {
        "_typename" to "TGeoBBox"
        "fDX" to xSize
        "fDY" to ySize
        "fDZ" to zSize
    }

    /**
     * Create a GDML union
     */
    operator fun Meta.plus(other: Meta) = buildMeta {
        "fNode.fLeft" to this
        "fNode.fRight" to other
        "fNode._typename" to "TGeoUnion"
    }

    /**
     * Create a GDML subtraction
     */
    operator fun Meta.minus(other: Meta)  = buildMeta {
        "fNode.fLeft" to this
        "fNode.fRight" to other
        "fNode._typename" to "TGeoSubtraction"
    }

    /**
     * Intersect two GDML geometries
     */
    infix fun Meta.intersect(other: Meta) = buildMeta {
        "fNode.fLeft" to this
        "fNode.fRight" to other
        "fNode._typename" to "TGeoIntersection"
    }

    companion object {
        const val TYPE = "geometry.spatial.jsRoot.geometry"
    }
}

fun DisplayGroup.jsRootGeometry(meta: Meta = EmptyMeta, action: JSRootGeometry.() -> Unit = {}) =
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