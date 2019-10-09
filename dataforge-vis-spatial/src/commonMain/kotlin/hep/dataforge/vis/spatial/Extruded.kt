@file:UseSerializers(Point2DSerializer::class, Point3DSerializer::class)
package hep.dataforge.vis.spatial

import hep.dataforge.io.ConfigSerializer
import hep.dataforge.meta.Config
import hep.dataforge.vis.common.AbstractVisualObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


typealias Shape2D = List<Point2D>

@Serializable
class Shape2DBuilder(private val points: MutableList<Point2D> = ArrayList()) {

    fun point(x: Number, y: Number) {
        points.add(Point2D(x, y))
    }

    infix fun Number.to(y: Number) = point(this, y)

    fun build(): Shape2D = points
}

fun Shape2DBuilder.polygon(vertices: Int, radius: Number) {
    require(vertices > 2) { "Polygon must have more than 2 vertices" }
    val angle = 2 * PI / vertices
    for (i in 0 until vertices) {
        point(radius.toDouble() * cos(angle * i), radius.toDouble() * sin(angle * i))
    }
}

@Serializable
data class Layer(var x: Float, var y: Float, var z: Float, var scale: Float)

@Serializable
class Extruded(
    var shape: List<Point2D> = ArrayList(),
    var layers: MutableList<Layer> = ArrayList()
) : AbstractVisualObject(), VisualObject3D, Shape {

    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    fun shape(block: Shape2DBuilder.() -> Unit) {
        this.shape = Shape2DBuilder().apply(block).build()
        //TODO send invalidation signal
    }

    fun layer(z: Number, x: Number = 0.0, y: Number = 0.0, scale: Number = 1.0) {
        layers.add(Layer(x.toFloat(), y.toFloat(), z.toFloat(), scale.toFloat()))
        //TODO send invalidation signal
    }

    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {
        val shape: Shape2D = shape

        if (shape.size < 3) error("Extruded shape requires more than 2 points per layer")

        /**
         * Expand the shape for specific layers
         */
        val layers: List<List<Point3D>> = layers.map { layer ->
            shape.map { (x, y) ->
                val newX = layer.x + x * layer.scale
                val newY = layer.y + y * layer.scale
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

fun VisualGroup3D.extrude(name: String = "", action: Extruded.() -> Unit = {}) =
    Extruded().apply(action).also { set(name, it) }