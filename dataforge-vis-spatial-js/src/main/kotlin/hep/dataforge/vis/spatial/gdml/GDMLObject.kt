package hep.dataforge.vis.spatial.gdml

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaItem
import hep.dataforge.vis.*
import hep.dataforge.vis.spatial.GenericThreeBuilder
import hep.dataforge.vis.spatial.Materials
import hep.dataforge.vis.spatial.material
import hep.dataforge.vis.spatial.three.EdgesGeometry
import info.laht.threekt.objects.LineSegments
import info.laht.threekt.objects.Mesh

class GDMLObject(parent: DisplayObject?, meta: Meta) : DisplayLeaf(parent, TYPE, meta) {

    var shape by node()

    var color by item()

    var facesLimit by int(0)

    companion object {
        const val TYPE = "geometry.spatial.gdml"
    }
}

//TODO add Zelenyy GDML builder here
fun DisplayGroup.gdml(meta: Meta = EmptyMeta, action: GDMLObject.() -> Unit = {}) =
    GDMLObject(this, meta).apply(action).also { addChild(it) }

fun Meta.toDynamic(): dynamic {
    fun MetaItem<*>.toDynamic(): dynamic = when (this) {
        is MetaItem.ValueItem -> this.value.value.asDynamic()
        is MetaItem.NodeItem -> this.node.toDynamic()
    }

    val res = js("{}")
    this.items.entries.groupBy { it.key.body }.forEach { (key, value) ->
        val list = value.map { it.value }
        res[key] = when (list.size) {
            1 -> list.first().toDynamic()
            else -> list.map { it.toDynamic() }
        }
    }
    return res
}


object ThreeGDMLBuilder : GenericThreeBuilder<GDMLObject, Mesh>() {
    override fun build(obj: GDMLObject): Mesh {
        val shapeMeta = obj.shape?.toDynamic() ?: error("The shape not defined")
        println(shapeMeta)
        val geometry = createGeometry(shapeMeta, obj.facesLimit)
        return Mesh(geometry, obj.color.material()).also { mesh ->
            mesh.add(LineSegments(EdgesGeometry(geometry), Materials.DEFAULT))
        }
    }

    override fun update(obj: GDMLObject, target: Mesh) {
        val shapeMeta: dynamic = obj.shape?.toDynamic()
        target.geometry = createGeometry(shapeMeta, obj.facesLimit)
        target.material = obj.color.material()
    }
}