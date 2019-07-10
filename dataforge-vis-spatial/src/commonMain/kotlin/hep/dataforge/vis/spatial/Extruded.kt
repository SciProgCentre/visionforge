package hep.dataforge.vis.spatial

import hep.dataforge.meta.*
import hep.dataforge.vis.common.DisplayLeaf
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

    infix fun Number.to(y:Number) = point(this, y)

    fun build(): Shape2D = list
}

fun Shape2DBuilder.polygon(vertices: Int, radius: Number) {
    require(vertices > 2) { "Polygon must have more than 2 vertices" }
    val angle = 2 * PI / vertices
    for (i in 0 until vertices) {
        point(radius.toDouble() * cos(angle * i), radius.toDouble() * sin(angle * i))
    }
}

class Layer(override val config: Config) : Specific {
    var z by number(0.0)
    var x by number(0.0)
    var y by number(0.0)
    var scale by number(1.0)

    companion object : Specification<Layer> {
        override fun wrap(config: Config): Layer = Layer(config)
    }
}

//class Layer(val z: Number, val x: Number = 0.0, val y: Number = 0.0, val scale: Number = 1.0)

class Extruded(parent: VisualObject?, meta: Meta) : DisplayLeaf(parent, meta), Shape {

    val shape
        get() = properties.getAll("shape.point").map { (_, value) ->
            Point2D.from(value.node ?: error("Point definition is not a node"))
        }

    fun shape(block: Shape2DBuilder.() -> Unit) {
        val points = Shape2DBuilder().apply(block).build().map { it.toMeta() }
        properties["shape.point"] = points
    }

    val layers
        get() = properties.getAll("layer").values.map {
            Layer.wrap(it.node ?: error("layer item is not a node"))
        }

    fun layer(z: Number, x: Number = 0.0, y: Number = 0.0, scale: Number = 1.0): Layer {
        val layer = Layer.build {
            this.x = x
            this.y = y
            this.z = z
            this.scale = scale
        }
        properties.append("layer", layer)
        return layer
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
    }

    companion object {
        const val TYPE = "geometry.3d.extruded"
    }
}

fun VisualGroup.extrude(meta: Meta = EmptyMeta, action: Extruded.() -> Unit = {}) =
    Extruded(this, meta).apply(action).also { add(it) }