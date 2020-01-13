package hep.dataforge.vis.spatial.fx

import hep.dataforge.meta.Meta
import hep.dataforge.vis.spatial.GeometryBuilder
import hep.dataforge.vis.spatial.Point3D
import hep.dataforge.vis.spatial.Shape
import javafx.scene.shape.Mesh
import javafx.scene.shape.MeshView
import javafx.scene.shape.TriangleMesh
import javafx.scene.shape.VertexFormat
import org.fxyz3d.geometry.Face3
import kotlin.reflect.KClass

object FXShapeFactory : FX3DFactory<Shape> {
    override val type: KClass<in Shape> get() = Shape::class

    override fun invoke(obj: Shape, binding: VisualObjectFXBinding): MeshView {
        val mesh = FXGeometryBuilder().apply { obj.toGeometry(this) }.build()
        return MeshView(mesh)
    }
}

private class FXGeometryBuilder : GeometryBuilder<Mesh> {
    val vertices = ArrayList<Point3D>()
    val faces = ArrayList<Face3>()
    private val vertexCache = HashMap<Point3D, Int>()

    private fun append(vertex: Point3D): Int {
        val index = vertexCache[vertex] ?: -1//vertices.indexOf(vertex)
        return if (index > 0) {
            index
        } else {
            vertices.add(vertex)
            vertexCache[vertex] = vertices.size - 1
            vertices.size - 1
        }
    }

    override fun face(vertex1: Point3D, vertex2: Point3D, vertex3: Point3D, normal: Point3D?, meta: Meta) {
        //adding vertices
        val face = Face3(append(vertex1), append(vertex2), append(vertex3))
        faces.add(face)
    }

    override fun build(): Mesh {
        val mesh = TriangleMesh(VertexFormat.POINT_TEXCOORD)
        vertices.forEach {
            //TODO optimize copy
            mesh.points.addAll(it.x.toFloat(), it.y.toFloat(), it.z.toFloat())
        }

        mesh.texCoords.addAll(0f, 0f)

        faces.forEach {
            mesh.faces.addAll(it.p0, 0, it.p1, 0, it.p2, 0)
        }
        return mesh
    }

}

