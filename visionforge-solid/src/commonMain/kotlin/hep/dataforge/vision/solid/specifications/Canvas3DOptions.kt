package hep.dataforge.vision.solid.specifications

import hep.dataforge.meta.*

public class Canvas3DOptions : Scheme() {
    public var axes: Axes by spec(Axes, Axes.empty())
    public var camera: Camera by spec(Camera, Camera.empty())
    public var controls: Controls by spec(Controls, Controls.empty())

    public var minSize: Int by int(300)
    public var minWith: Number by number { minSize }
    public var minHeight: Number by number { minSize }

    public var maxSize: Int by int(Int.MAX_VALUE)
    public var maxWith: Number by number { maxSize }
    public var maxHeight: Number by number { maxSize }


    public companion object : SchemeSpec<Canvas3DOptions>(::Canvas3DOptions)
}