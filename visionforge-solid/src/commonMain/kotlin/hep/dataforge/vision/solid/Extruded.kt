package hep.dataforge.vision.solid

import hep.dataforge.vision.VisionBuilder
import hep.dataforge.vision.VisionContainerBuilder
import hep.dataforge.vision.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


public typealias Shape2D = List<Point2D>

@Serializable
public class Shape2DBuilder(private val points: MutableList<Point2D> = ArrayList()) {

    public fun point(x: Number, y: Number) {
        points.add(Point2D(x, y))
    }

    public infix fun Number.to(y: Number): Unit = point(this, y)

    public fun build(): Shape2D = points
}

public fun Shape2DBuilder.polygon(vertices: Int, radius: Number) {
    require(vertices > 2) { "Polygon must have more than 2 vertices" }
    val angle = 2 * PI / vertices
    for (i in 0 until vertices) {
        point(radius.toDouble() * cos(angle * i), radius.toDouble() * sin(angle * i))
    }
}

@Serializable
public data class Layer(var x: Float, var y: Float, var z: Float, var scale: Float)

@Serializable
@SerialName("solid.extrude")
public class Extruded(
    public var shape: List<Point2D> = ArrayList(),
    public var layers: MutableList<Layer> = ArrayList()
) : SolidBase(), GeometrySolid {

    public fun shape(block: Shape2DBuilder.() -> Unit) {
        this.shape = Shape2DBuilder().apply(block).build()
        //TODO send invalidation signal
    }

    public fun layer(z: Number, x: Number = 0.0, y: Number = 0.0, scale: Number = 1.0) {
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

    public companion object {
        public const val TYPE: String = "solid.extruded"
    }
}

@VisionBuilder
public fun VisionContainerBuilder<Solid>.extrude(name: String = "", action: Extruded.() -> Unit = {}): Extruded =
    Extruded().apply(action).also { set(name, it) }