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

internal fun <T> MutableList<T>.add(f1: T, f2: T, f3: T) {
    add(f1)
    add(f2)
    add(f3)
}

/**
 * An implementation of geometry builder for Three.js [BufferGeometry]
 */
public class ThreeGeometryBuilder : GeometryBuilder<BufferGeometry> {

    private val indices = ArrayList<Short>()
    private val positions = ArrayList<Float>()
    private val normals = ArrayList<Float>()
    private val colors = ArrayList<Float>()

    private val vertexCache = HashMap<Point3D, Short>()
    private var counter: Short = -1

    private fun indexOf(vertex: Point3D, normal: Point3D): Short = vertexCache.getOrPut(vertex) {
        //add vertex and update cache if needed
        positions.add(vertex.x, vertex.y, vertex.z)
        normals.add(normal.x, vertex.y, vertex.z)
        colors.add(1f, 1f, 1f)
        counter++
        counter
    }

    override fun face(vertex1: Point3D, vertex2: Point3D, vertex3: Point3D, normal: Point3D?, meta: Meta) {
        val actualNormal: Point3D = normal ?: (vertex3 - vertex2) cross (vertex1 - vertex2)
        indices.add(
            indexOf(vertex1, actualNormal),
            indexOf(vertex2, actualNormal),
            indexOf(vertex3, actualNormal)
        )
    }


    override fun build(): BufferGeometry = BufferGeometry().apply {
        //setIndex(Int16BufferAttribute(indices.toShortArray(), 1))
        setIndex(indices.toTypedArray())
        setAttribute("position", Float32BufferAttribute(positions.toTypedArray(), 3))
        setAttribute("normal", Float32BufferAttribute(normals.toTypedArray(), 3))
        //setAttribute("color", Float32BufferAttribute(colors.toFloatArray(), 3))

        computeBoundingSphere()
    }
}