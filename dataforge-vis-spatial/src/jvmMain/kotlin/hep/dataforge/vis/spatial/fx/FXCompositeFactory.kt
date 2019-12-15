package hep.dataforge.vis.spatial.fx

import eu.mihosoft.jcsg.CSG
import hep.dataforge.vis.spatial.Composite
import hep.dataforge.vis.spatial.CompositeType
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.shape.MeshView
import org.fxyz3d.utils.MeshUtils
import kotlin.reflect.KClass

class FXCompositeFactory(val plugin: FX3DPlugin) :
    FX3DFactory<Composite> {
    override val type: KClass<in Composite>
        get() = Composite::class

    override fun invoke(obj: Composite, binding: VisualObjectFXBinding): Node {
        val first = plugin.buildNode(obj.first) as? MeshView ?: error("Can't build node")
        val second = plugin.buildNode(obj.second) as? MeshView ?: error("Can't build node")
        val firstCSG = MeshUtils.mesh2CSG(first)
        val secondCSG = MeshUtils.mesh2CSG(second)
        val resultCSG = when(obj.compositeType){
            CompositeType.UNION -> firstCSG.union(secondCSG)
            CompositeType.INTERSECT -> firstCSG.intersect(secondCSG)
            CompositeType.SUBTRACT -> firstCSG.difference(secondCSG)
        }
        return resultCSG.toNode()
    }
}

internal fun CSG.toNode(): Node{
    val meshes = toJavaFXMesh().asMeshViews
    return if(meshes.size == 1){
        meshes.first()
    } else {
        Group(meshes.map { it })
    }
}