package hep.dataforge.vis.spatial.specifications

import hep.dataforge.meta.scheme.Scheme
import hep.dataforge.meta.scheme.SchemeSpec
import hep.dataforge.meta.scheme.int
import hep.dataforge.meta.scheme.spec

class Canvas : Scheme() {
    var axes by spec(Axes, Axes.empty())
    var camera by spec(Camera, Camera.empty())
    var controls by spec(Controls, Controls.empty())
    var minSize by int(300)

    companion object : SchemeSpec<Canvas>(::Canvas)
}