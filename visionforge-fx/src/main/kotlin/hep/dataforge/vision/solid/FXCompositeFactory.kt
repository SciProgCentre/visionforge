package hep.dataforge.vision.solid

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Polygon
import eu.mihosoft.vvecmath.Vector3d
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.shape.MeshView
import javafx.scene.shape.TriangleMesh
import javafx.scene.shape.VertexFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

private fun MeshView.toCSG(): CSG {
    val mesh = this.mesh as TriangleMesh
    if (mesh.vertexFormat != VertexFormat.POINT_TEXCOORD) error("Not POINT_TEXCOORD")
    val polygons: MutableList<Polygon> = ArrayList()
    val faces = mesh.faces
    val points = mesh.points

    val vectorCache = HashMap<Int, Vector3d>()
    fun getVector(index: Int) = vectorCache.getOrPut(index) {
        Vector3d.xyz(
            points[3 * index].toDouble(),
            points[3 * index + 1].toDouble(),
            points[3 * index + 2].toDouble()
        )
    }

    for (i in 0 until faces.size() / 6) {
        val polygon = Polygon.fromPoints(
            getVector(faces[6 * i]),
            getVector(faces[6 * i + 2]),
            getVector(faces[6 * i + 4])
        )
        polygons.add(polygon)
    }

    return CSG.fromPolygons(polygons)
}

class FXCompositeFactory(val plugin: FX3DPlugin) : FX3DFactory<Composite> {
    override val type: KClass<in Composite>
        get() = Composite::class

    override fun invoke(obj: Composite, binding: VisualObjectFXBinding): Node {
        val first = plugin.buildNode(obj.first) as? MeshView ?: error("Can't build node")
        val second = plugin.buildNode(obj.second) as? MeshView ?: error("Can't build node")
        val firstCSG = first.toCSG()
        val secondCSG = second.toCSG()
        val resultCSG = when (obj.compositeType) {
            CompositeType.UNION -> firstCSG.union(secondCSG)
            CompositeType.INTERSECT -> firstCSG.intersect(secondCSG)
            CompositeType.SUBTRACT -> firstCSG.difference(secondCSG)
        }
        return resultCSG.toNode()
    }
}

internal fun CSG.toNode(): Node {
    val meshes = toJavaFXMesh().asMeshViews
    return if (meshes.size == 1) {
        meshes.first()
    } else {
        Group(meshes.map { it })
    }
}