package hep.dataforge.vision.solid.specifications

import hep.dataforge.meta.Scheme
import hep.dataforge.meta.SchemeSpec
import hep.dataforge.meta.int
import hep.dataforge.meta.spec

public class Canvas3DOptions : Scheme() {
    public var axes: Axes by spec(Axes, Axes.empty())
    public var camera: Camera by spec(Camera, Camera.empty())
    public var controls: Controls by spec(Controls, Controls.empty())
    public var minSize: Int by int(300)

    public companion object : SchemeSpec<Canvas3DOptions>(::Canvas3DOptions)
}