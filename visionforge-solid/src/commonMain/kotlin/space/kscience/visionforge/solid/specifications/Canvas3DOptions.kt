package space.kscience.visionforge.solid.specifications

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.scheme
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.meta.set
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.hide
import space.kscience.visionforge.widgetType


public object Clipping : SchemeSpec<PointScheme>(::PointScheme) {
    override val descriptor: MetaDescriptor = MetaDescriptor {
        value(PointScheme::x) {
            widgetType = "range"
            attributes["min"] = 0.0
            attributes["max"] = 1.0
            attributes["step"] = 0.01
            default(1.0)
        }
        value(PointScheme::y) {
            widgetType = "range"
            attributes["min"] = 0.0
            attributes["max"] = 1.0
            attributes["step"] = 0.01
            default(1.0)
        }
        value(PointScheme::z) {
            widgetType = "range"
            attributes["min"] = 0.0
            attributes["max"] = 1.0
            attributes["step"] = 0.01
            default(1.0)
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
    @Suppress("DEPRECATION")
    public var axes: AxesScheme by spec(AxesScheme)
    public var camera: CameraScheme by spec(CameraScheme)
    public var controls: Canvas3DUIScheme by spec(Canvas3DUIScheme)

    public var size: CanvasSize by spec(CanvasSize)

    public var layers: List<Number> by numberList(0)

    public var clipping: PointScheme by spec(Clipping)

    public var onSelect: ((Name?) -> Unit)? = null


    public companion object : SchemeSpec<Canvas3DOptions>(::Canvas3DOptions) {
        override val descriptor: MetaDescriptor by lazy {
            MetaDescriptor {
                @Suppress("DEPRECATION")
                scheme(Canvas3DOptions::axes, AxesScheme)

                value(Canvas3DOptions::layers) {
                    multiple = true
                    default(listOf(0))
                    widgetType = "multiSelect"
                    allowedValues(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                }

                scheme(Canvas3DOptions::clipping, Clipping)

                scheme(Canvas3DOptions::camera, CameraScheme) {
                    hide()
                }

                scheme(Canvas3DOptions::controls, Canvas3DUIScheme) {
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
