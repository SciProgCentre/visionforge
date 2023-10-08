package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.names.Name
import space.kscience.kmath.geometry.component1
import space.kscience.kmath.geometry.component2
import space.kscience.kmath.structures.Float32
import space.kscience.visionforge.MutableVisionContainer
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.setChild


private inline fun <T> Iterable<T>.sumOf(selector: (T) -> Float32): Float32 {
    var sum = 0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

/**
 * A layered solid with a hole inside
 */
@Serializable
@SerialName("solid.surface")
public class Surface(
    public val layers: List<Layer>,
) : SolidBase<Surface>(), GeometrySolid {

    @Serializable
    public data class Layer(val z: Float32, val outer: Shape2D, val inner: Shape2D?) {
        init {
            require(outer.size >= 3) { "Extruded shape requires more than 2 points per layer" }
            require(inner == null || inner.size == outer.size) { "Outer shape size is ${outer.size}, but inner is ${inner?.size}" }
        }

        public fun outerPoints(): List<Float32Vector3D> = outer.map { (x, y) -> Float32Vector3D(x, y, z) }

        public fun innerPoints(): List<Float32Vector3D>? = inner?.map { (x, y) -> Float32Vector3D(x, y, z) }

        public val center: Float32Vector3D by lazy {
            Float32Vector3D(
                outer.sumOf { it.x } / size,
                outer.sumOf { it.y } / size,
                z
            )
        }


        val size: Int get() = outer.size
    }

    init {
        require(layers.size > 1) { "Number of layers must be 2 or more" }
        require(layers.all { it.size == layers.first().size }) { "All layers must have the same size" }
    }

    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {

        geometryBuilder.apply {
            //outer and inner
            for (i in 0 until layers.size - 1) {
                val bottom = layers[i]
                val top = layers[i + 1]

                //creating shape in x-y plane with z = 0
                val bottomOuterPoints = bottom.outerPoints()
                val topOuterPoints = top.outerPoints()

                for (it in 1 until bottomOuterPoints.size) {
                    //outer face
                    face4(
                        bottomOuterPoints[it - 1],
                        bottomOuterPoints[it],
                        topOuterPoints[it],
                        topOuterPoints[it - 1]
                    )
                }

                //outer face last segment
                face4(bottomOuterPoints.last(), bottomOuterPoints[0], topOuterPoints[0], topOuterPoints.last())

                val bottomInnerPoints = bottom.innerPoints() ?: bottom.outerPoints().map { bottom.center }
                val topInnerPoints = top.innerPoints() ?: top.outerPoints().map { top.center }

                for (it in 1 until bottomInnerPoints.size) {
                    //inner face
                    face4(
                        bottomInnerPoints[it],
                        bottomInnerPoints[it - 1],
                        topInnerPoints[it - 1],
                        topInnerPoints[it]
                    )
                }

                //inner face last segment
                face4(bottomInnerPoints[0], bottomInnerPoints.last(), topInnerPoints.last(), topInnerPoints[0])
            }

            val bottom = layers.first()
            val top = layers.last()
            val bottomOuterPoints = bottom.outerPoints()
            val topOuterPoints = top.outerPoints()
            val bottomInnerPoints = bottom.innerPoints() ?: bottom.outerPoints().map { bottom.center }
            val topInnerPoints = top.innerPoints() ?: top.outerPoints().map { top.center }

            (1 until bottom.size).forEach {

                //bottom cup
                face4(
                    bottomInnerPoints[it - 1],
                    bottomInnerPoints[it],
                    bottomOuterPoints[it],
                    bottomOuterPoints[it - 1]
                )
                //upper cup
                face4(
                    topInnerPoints[it],
                    topInnerPoints[it - 1],
                    topOuterPoints[it - 1],
                    topOuterPoints[it]
                )


                face4(
                    bottomInnerPoints.last(),
                    bottomInnerPoints[0],
                    bottomOuterPoints[0],
                    bottomOuterPoints.last()
                )
                face4(topInnerPoints[0], topInnerPoints.last(), topOuterPoints.last(), topOuterPoints[0])
            }
        }
    }

    public class Builder(
        public var layers: MutableList<Layer> = ArrayList(),
        public val properties: MutableMeta = MutableMeta(),
    ) {

        public fun layer(
            z: Number,
            innerBuilder: (Shape2DBuilder.() -> Unit)? = null,
            outerBuilder: Shape2DBuilder.() -> Unit,
        ) {
            layers.add(
                Layer(
                    z.toFloat(),
                    outer = Shape2DBuilder().apply(outerBuilder).build(),
                    inner = innerBuilder?.let { Shape2DBuilder().apply(innerBuilder).build() }
                )
            )
        }

        internal fun build(): Surface = Surface(layers).apply {
            properties.setMeta(Name.EMPTY, this@Builder.properties)
        }
    }


    public companion object {
        public const val TYPE: String = "solid.surface"
    }
}


@VisionBuilder
public fun MutableVisionContainer<Solid>.surface(
    name: String? = null,
    action: Surface.Builder.() -> Unit = {},
): Surface = Surface.Builder().apply(action).build().also { setChild(name, it) }
