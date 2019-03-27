package hep.dataforge.vis.spatial.jsroot

import hep.dataforge.meta.DynamicMeta
import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.meta.toDynamic
import hep.dataforge.vis.DisplayGroup
import hep.dataforge.vis.DisplayLeaf
import hep.dataforge.vis.DisplayObject
import hep.dataforge.vis.node
import hep.dataforge.vis.spatial.ThreeFactory
import info.laht.threekt.core.Object3D

class JSRootObject(parent: DisplayObject?, meta: Meta) : DisplayLeaf(parent, TYPE, meta) {

    var data by node()
    var options by node()

    companion object {
        const val TYPE = "geometry.spatial.jsRoot.object"
    }
}

object JSRootObjectFactory : ThreeFactory<JSRootObject> {

    override val type = JSRootObject::class

    override fun invoke(obj: JSRootObject): Object3D {
        return build(obj.data?.toDynamic(), obj.options?.toDynamic())
    }
}

fun DisplayGroup.jsRoot(path: String) {
    JSRootObject(this, EmptyMeta).apply{
        data = DynamicMeta(hep.dataforge.vis.require(path))
    }.also { addChild(it) }
}