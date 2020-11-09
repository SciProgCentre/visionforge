package hep.dataforge.vision.solid.specifications

import hep.dataforge.meta.*

public class Canvas3DOptions : Scheme() {
    public var axes: Axes by spec(Axes, Axes.empty())
    public var camera: Camera by spec(Camera, Camera.empty())
    public var controls: Controls by spec(Controls, Controls.empty())

    public var minSize: Int by int(400)
    public var minWith: Number by number { minSize }
    public var minHeight: Number by number { minSize }

    public var maxSize: Int by int(Int.MAX_VALUE)
    public var maxWith: Number by number { maxSize }
    public var maxHeight: Number by number { maxSize }


    public companion object : SchemeSpec<Canvas3DOptions>(::Canvas3DOptions)
}

public fun Canvas3DOptions.computeWidth(external: Number): Int =
    (external.toInt()).coerceIn(minWith.toInt()..maxWith.toInt())

public fun Canvas3DOptions.computeHeight(external: Number): Int =
    (external.toInt()).coerceIn(minHeight.toInt()..maxHeight.toInt())