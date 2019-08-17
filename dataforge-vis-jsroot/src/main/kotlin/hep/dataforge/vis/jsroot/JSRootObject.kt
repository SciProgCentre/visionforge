package hep.dataforge.vis.jsroot

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.meta.toDynamic
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.DisplayLeaf
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.node
import hep.dataforge.vis.spatial.three.ThreeFactory
import info.laht.threekt.core.Object3D

class JSRootObject(parent: VisualObject?, meta: Meta, val data: dynamic) : DisplayLeaf(parent, meta) {

    var options by node()

    companion object {
        const val TYPE = "geometry.spatial.jsRoot.object"
    }
}

object ThreeJSRootObjectFactory : ThreeFactory<JSRootObject> {

    override val type = JSRootObject::class

    override fun invoke(obj: JSRootObject): Object3D {
        return build(obj.data, obj.options?.toDynamic())
    }
}

fun VisualGroup.jsRootObject(str: String) {
    val json = JSON.parse<Any>(str)
    JSRootObject(this, EmptyMeta, json).also { add(it) }
}