package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.names.Name
import space.kscience.kmath.geometry.component1
import space.kscience.kmath.geometry.component2
import space.kscience.visionforge.MutableVisionContainer
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.setChild
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


public typealias Shape2D = List<Float32Vector2D>

@Serializable
public class Shape2DBuilder(private val points: ArrayList<Float32Vector2D> = ArrayList()) {

    public fun point(x: Number, y: Number) {
        points.add(Float32Vector2D(x, y))
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
    public val shape: List<Float32Vector2D>,
    public val layers: List<Layer>,
) : SolidBase<Extruded>(), GeometrySolid {

    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {
        val shape: Shape2D = shape

        if (shape.size < 3) error("Extruded shape requires more than 2 points per layer")

        /**
         * Expand the shape for specific layers
         */
        val layers: List<List<Float32Vector3D>> = layers.map { layer ->
            shape.map { (x, y) ->
                val newX = layer.x + x * layer.scale
                val newY = layer.y + y * layer.scale
                Float32Vector3D(newX, newY, layer.z)
            }
        }

        if (layers.size < 2) error("Extruded shape requires more than one layer")

        var lowerLayer = layers.first()
        var upperLayer: List<Float32Vector3D>

        for (i in (1 until layers.size)) {
            upperLayer = layers[i]
            for (j in (0 until shape.size - 1)) {
                //counterclockwise
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

public class ExtrudeBuilder(
    public var shape: List<Float32Vector2D> = emptyList(),
    public var layers: MutableList<Layer> = ArrayList(),
    public val properties: MutableMeta = MutableMeta(),
) {
    public fun shape(block: Shape2DBuilder.() -> Unit) {
        this.shape = Shape2DBuilder().apply(block).build()
    }

    public fun layer(z: Number, x: Number = 0.0, y: Number = 0.0, scale: Number = 1.0) {
        layers.add(Layer(x.toFloat(), y.toFloat(), z.toFloat(), scale.toFloat()))
    }

    internal fun build(): Extruded = Extruded(shape, layers).apply {
        this.properties.setProperty(Name.EMPTY, this@ExtrudeBuilder.properties)
    }
}

@VisionBuilder
public fun MutableVisionContainer<Solid>.extruded(
    name: String? = null,
    action: ExtrudeBuilder.() -> Unit = {},
): Extruded = ExtrudeBuilder().apply(action).build().also { setChild(name, it) }