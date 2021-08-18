package space.kscience.visionforge.solid.three

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Float32BufferAttribute
import info.laht.threekt.math.Vector3
import space.kscience.dataforge.meta.Meta
import space.kscience.visionforge.solid.GeometryBuilder
import space.kscience.visionforge.solid.Point3D
import space.kscience.visionforge.solid.cross
import space.kscience.visionforge.solid.minus

internal fun Point3D.toVector() = Vector3(x, y, z)

internal fun <T> MutableList<T>.add(vararg values: T) {
    values.forEach {
        add(it)
    }
}

/**
 * An implementation of geometry builder for Three.js [BufferGeometry]
 */
public class ThreeGeometryBuilder : GeometryBuilder<BufferGeometry> {

    private val indices = ArrayList<Short>()
    private val positions = ArrayList<Float>()
    private val normals = ArrayList<Float>()
//    private val colors = ArrayList<Float>()

    private val vertexCache = HashMap<Point3D, Short>()
    private var counter: Short = -1

    private fun vertex(vertex: Point3D, normal: Point3D): Short = vertexCache.getOrPut(vertex) {
        //add vertex and update cache if needed
        positions.add(vertex.x, vertex.y, vertex.z)
        normals.add(normal.x, vertex.y, vertex.z)
        //colors.add(1f, 1f, 1f)
        counter++
        counter
    }

    override fun face(vertex1: Point3D, vertex2: Point3D, vertex3: Point3D, normal: Point3D?, meta: Meta) {
        val actualNormal: Point3D = normal ?: ((vertex3 - vertex2) cross (vertex1 - vertex2))
        indices.add(
            vertex(vertex1, actualNormal),
            vertex(vertex2, actualNormal),
            vertex(vertex3, actualNormal)
        )
    }


    override fun build(): BufferGeometry = BufferGeometry().apply {
        setIndex(indices.toTypedArray())
        setAttribute("position", Float32BufferAttribute(positions.toTypedArray(), 3))
        setAttribute("normal", Float32BufferAttribute(normals.toTypedArray(), 3))
        //setAttribute("color", Float32BufferAttribute(colors.toFloatArray(), 3))
        //a temporary fix for CSG problem
        val uvsArray = Array<Float>((counter+1)*2){0f}
        setAttribute("uv", Float32BufferAttribute(uvsArray, 2))

        computeBoundingSphere()
    }
}