package space.kscience.visionforge.solid.specifications

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.scheme
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.values.ValueType
import space.kscience.visionforge.hide
import space.kscience.visionforge.widgetType

public class Clipping : Scheme() {
    public var x: Double? by double()
    public var y: Double? by double()
    public var z: Double? by double()

    public companion object : SchemeSpec<Clipping>(::Clipping) {
        override val descriptor: MetaDescriptor = MetaDescriptor {
            value(Clipping::x) {
                widgetType = "range"
                attributes["min"] = 0.0
                attributes["max"] = 1.0
                attributes["step"] = 0.01
            }
            value(Clipping::y) {
                widgetType = "range"
                attributes["min"] = 0.0
                attributes["max"] = 1.0
                attributes["step"] = 0.01

            }
            value(Clipping::z) {
                widgetType = "range"
                attributes["min"] = 0.0
                attributes["max"] = 1.0
                attributes["step"] = 0.01

            }
        }
    }
}

public class CanvasSize : Scheme() {
    public var minSize: Int by int(400)
    public var minWith: Number by number { minSize }
    public var minHeight: Number by number { minSize }

    public var maxSize: Int by int(Int.MAX_VALUE)
    public var maxWith: Number by number { maxSize }
    public var maxHeight: Number by number { maxSize }

    public companion object : SchemeSpec<CanvasSize>(::CanvasSize) {
        override val descriptor: MetaDescriptor = MetaDescriptor {
            value(CanvasSize::minSize)
            value(CanvasSize::minWith)
            value(CanvasSize::minHeight)
            value(CanvasSize::maxSize)
            value(CanvasSize::maxWith)
            value(CanvasSize::maxHeight)
        }
    }
}

public class Canvas3DOptions : Scheme() {
    public var axes: Axes by spec(Axes)
    public var light: Light by spec(Light)
    public var camera: Camera by spec(Camera)
    public var controls: Controls by spec(Controls)

    public var size: CanvasSize by spec(CanvasSize)

    public var layers: List<Number> by numberList(0)

    public var clipping: Clipping by spec(Clipping)

    public var onSelect: ((Name?) -> Unit)? = null


    public companion object : SchemeSpec<Canvas3DOptions>(::Canvas3DOptions) {
        override val descriptor: MetaDescriptor by lazy {
            MetaDescriptor {
                scheme(Canvas3DOptions::axes, Axes)

                value(Canvas3DOptions::layers) {
                    multiple = true
                    default(listOf(0))
                    widgetType = "multiSelect"
                    allowedValues(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                }

                scheme(Canvas3DOptions::clipping, Clipping)

                scheme(Canvas3DOptions::light, Light){
                    hide()
                }

                scheme(Canvas3DOptions::camera, Camera) {
                    hide()
                }

                scheme(Canvas3DOptions::controls, Controls) {
                    hide()
                }

                scheme(Canvas3DOptions::size, CanvasSize) {
                    hide()
                }
            }
        }
    }
}

public fun CanvasSize.computeWidth(external: Number): Int =
    (external.toInt()).coerceIn(minWith.toInt()..maxWith.toInt())

public fun CanvasSize.computeHeight(external: Number): Int =
    (external.toInt()).coerceIn(minHeight.toInt()..maxHeight.toInt())
