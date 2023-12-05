package space.kscience.visionforge.solid.three

import space.kscience.dataforge.meta.Meta
import space.kscience.visionforge.solid.Float32Euclidean3DSpace
import space.kscience.visionforge.solid.Float32Vector3D
import space.kscience.visionforge.solid.GeometryBuilder
import three.core.BufferGeometry
import three.core.Float32BufferAttribute
import three.math.Vector3

internal fun Float32Vector3D.toVector() = Vector3(x, y, z)

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

    private val vertexCache = HashMap<Float32Vector3D, Short>()
    private var counter: Short = -1

    private fun vertex(vertex: Float32Vector3D, normal: Float32Vector3D): Short = vertexCache.getOrPut(vertex) {
        //add vertex and update cache if needed
        positions.add(vertex.x, vertex.y, vertex.z)
        normals.add(normal.x, vertex.y, vertex.z)
        //colors.add(1f, 1f, 1f)
        counter++
        counter
    }

    override fun face(
        vertex1: Float32Vector3D,
        vertex2: Float32Vector3D,
        vertex3: Float32Vector3D,
        normal: Float32Vector3D?,
        meta: Meta,
    ) = with(Float32Euclidean3DSpace) {
        val actualNormal: Float32Vector3D = normal ?: ((vertex3 - vertex2) cross (vertex1 - vertex2))
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