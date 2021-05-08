package space.kscience.visionforge.solid.specifications

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.NodeDescriptor
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.values.ValueType
import space.kscience.visionforge.hide
import space.kscience.visionforge.scheme
import space.kscience.visionforge.value
import space.kscience.visionforge.widgetType

public class Canvas3DOptions : Scheme() {
    public var axes: Axes by spec(Axes)
    public var light: Light by spec(Light)
    public var camera: Camera by spec(Camera)
    public var controls: Controls by spec(Controls)

    public var minSize: Int by int(400)
    public var minWith: Number by number { minSize }
    public var minHeight: Number by number { minSize }

    public var maxSize: Int by int(Int.MAX_VALUE)
    public var maxWith: Number by number { maxSize }
    public var maxHeight: Number by number { maxSize }

    public var layers: List<Number> by numberList(0)

    public var onSelect: ((Name?) -> Unit)? = null


    public companion object : SchemeSpec<Canvas3DOptions>(::Canvas3DOptions) {
        override val descriptor: NodeDescriptor by lazy {
            NodeDescriptor {
                scheme(Canvas3DOptions::axes, Axes)
                scheme(Canvas3DOptions::light, Light)
                scheme(Canvas3DOptions::camera, Camera) {
                    hide()
                }
                scheme(Canvas3DOptions::controls, Controls) {
                    hide()
                }
                value(Canvas3DOptions::minSize) {
                    hide()
                }
                value(Canvas3DOptions::minWith) {
                    hide()
                }
                value(Canvas3DOptions::minHeight) {
                    hide()
                }
                value(Canvas3DOptions::maxSize) {
                    hide()
                }
                value(Canvas3DOptions::maxWith) {
                    hide()
                }
                value(Canvas3DOptions::maxHeight) {
                    hide()
                }
                value(Canvas3DOptions::layers) {
                    type(ValueType.NUMBER)
                    multiple = true
                    default(listOf(0))
                    widgetType = "multiSelect"
                    allow(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                }
            }
        }
    }
}

public fun Canvas3DOptions.computeWidth(external: Number): Int =
    (external.toInt()).coerceIn(minWith.toInt()..maxWith.toInt())

public fun Canvas3DOptions.computeHeight(external: Number): Int =
    (external.toInt()).coerceIn(minHeight.toInt()..maxHeight.toInt())
