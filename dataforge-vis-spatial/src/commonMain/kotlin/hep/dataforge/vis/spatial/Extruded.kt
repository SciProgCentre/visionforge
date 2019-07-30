package hep.dataforge.vis.spatial

import hep.dataforge.meta.Meta
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


typealias Shape2D = List<Point2D>

class Shape2DBuilder {
    private val list = ArrayList<Point2D>()

    fun point(x: Number, y: Number) {
        list.add(Point2D(x, y))
    }

    infix fun Number.to(y: Number) = point(this, y)

    fun build(): Shape2D = list
}

fun Shape2DBuilder.polygon(vertices: Int, radius: Number) {
    require(vertices > 2) { "Polygon must have more than 2 vertices" }
    val angle = 2 * PI / vertices
    for (i in 0 until vertices) {
        point(radius.toDouble() * cos(angle * i), radius.toDouble() * sin(angle * i))
    }
}

data class Layer(var x: Number, var y: Number, var z: Number, var scale: Number)

class Extruded(parent: VisualObject?, meta: Array<out Meta>) : VisualLeaf3D(parent, meta), Shape {

    var shape: List<Point2D> = ArrayList()

    fun shape(block: Shape2DBuilder.() -> Unit) {
        this.shape = Shape2DBuilder().apply(block).build()
        //TODO send invalidation signal
    }

    val layers: MutableList<Layer> = ArrayList()

    fun layer(z: Number, x: Number = 0.0, y: Number = 0.0, scale: Number = 1.0) {
        layers.add(Layer(x, y, z, scale))
        //TODO send invalidation signal
    }

    private fun <T : Any> GeometryBuilder<T>.cap(shape: List<Point3D>) {
        //FIXME won't work for non-convex shapes
        val center = Point3D(
            shape.map { it.x.toDouble() }.average(),
            shape.map { it.y.toDouble() }.average(),
            shape.map { it.z.toDouble() }.average()
        )
        for(i in 0 until (shape.size - 1)){
            face(shape[i], shape[i+1], center, null)
        }
        face(shape.last(), shape.first(),center,null)
    }

    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {
        val shape: Shape2D = shape

        if (shape.size < 3) error("Extruded shape requires more than points per layer")

        /**
         * Expand the shape for specific layers
         */
        val layers: List<List<Point3D>> = layers.map { layer ->
            shape.map { (x, y) ->
                val newX = layer.x.toDouble() + x.toDouble() * layer.scale.toDouble()
                val newY = layer.y.toDouble() + y.toDouble() * layer.scale.toDouble()
                Point3D(newX, newY, layer.z)
            }
        }

        if (layers.size < 2) error("Extruded shape requires more than one layer")

        var lowerLayer = layers.first()
        var upperLayer: List<Point3D>

        for (i in (1 until layers.size)) {
            upperLayer = layers[i]
            for (j in (0 until shape.size - 1)) {
                //counter clockwise
                geometryBuilder.face4(
                    lowerLayer[j],
                    lowerLayer[j + 1],
                    upperLayer[j + 1],
                    upperLayer[j]
                )
            }

            // final face
            geometryBuilder.face4(
                lowerLayer[shape.size - 1],
                lowerLayer[0],
                upperLayer[0],
                upperLayer[shape.size - 1]
            )
            lowerLayer = upperLayer
        }
        geometryBuilder.cap(layers.first().reversed())
        geometryBuilder.cap(layers.last())
    }

    companion object {
        const val TYPE = "geometry.3d.extruded"
    }
}

fun VisualGroup.extrude(name: String? = null, vararg meta: Meta, action: Extruded.() -> Unit = {}) =
    Extruded(this, meta).apply(action).also { set(name, it) }