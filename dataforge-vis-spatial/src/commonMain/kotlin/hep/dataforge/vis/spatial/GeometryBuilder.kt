package hep.dataforge.vis.spatial

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta

/**
 * @param T the type of resulting geometry
 */
interface GeometryBuilder<T : Any> {
    /**
     * Add a face to 3D model. If one of the vertices is not present in the current geometry model list of vertices,
     * it is added automatically.
     *
     * @param normal optional external normal to the face
     * @param meta optional additional platform-specific parameters like color or texture index
     */
    fun face(vertex1: Point3D, vertex2: Point3D, vertex3: Point3D, normal: Point3D? = null, meta: Meta = EmptyMeta)

    fun build(): T
}

fun GeometryBuilder<*>.face4(
    vertex1: Point3D,
    vertex2: Point3D,
    vertex3: Point3D,
    vertex4: Point3D,
    normal: Point3D? = null,
    meta: Meta = EmptyMeta
) {
    face(vertex1, vertex2, vertex3, normal, meta)
    face(vertex1, vertex3, vertex4, normal, meta)
}

interface Shape : VisualObject3D {
    fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>)
}

fun <T : Any> GeometryBuilder<T>.cap(shape: List<Point3D>, normal: Point3D? = null) {
    //FIXME won't work for non-convex shapes
    val center = Point3D(
        shape.map { it.x }.average(),
        shape.map { it.y }.average(),
        shape.map { it.z }.average()
    )
    for (i in 0 until (shape.size - 1)) {
        face(shape[i], shape[i + 1], center, normal)
    }
    face(shape.last(), shape.first(), center, normal)
}