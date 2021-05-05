package space.kscience.visionforge.solid.specifications

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.NodeDescriptor
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.scheme
import space.kscience.visionforge.value

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

    public var onSelect: ((Name?) -> Unit)? = null


    public companion object : SchemeSpec<Canvas3DOptions>(::Canvas3DOptions) {
        override val descriptor: NodeDescriptor by lazy {
            NodeDescriptor {
                scheme(Canvas3DOptions::axes, Axes)
                scheme(Canvas3DOptions::light, Light)
                scheme(Canvas3DOptions::camera, Camera)
                scheme(Canvas3DOptions::controls, Controls)
                value(Canvas3DOptions::minSize)
                value(Canvas3DOptions::minWith)
                value(Canvas3DOptions::minHeight)
                value(Canvas3DOptions::maxSize)
                value(Canvas3DOptions::maxWith)
                value(Canvas3DOptions::maxHeight)
            }
        }
    }
}

public fun Canvas3DOptions.computeWidth(external: Number): Int =
    (external.toInt()).coerceIn(minWith.toInt()..maxWith.toInt())

public fun Canvas3DOptions.computeHeight(external: Number): Int =
    (external.toInt()).coerceIn(minHeight.toInt()..maxHeight.toInt())
