package hep.dataforge.vis.spatial.specifications

import hep.dataforge.meta.Scheme
import hep.dataforge.meta.SchemeSpec
import hep.dataforge.meta.int
import hep.dataforge.meta.spec

class Canvas : Scheme() {
    var axes by spec(Axes, Axes.empty())
    var camera by spec(Camera, Camera.empty())
    var controls by spec(Controls, Controls.empty())
    var minSize by int(300)

    companion object : SchemeSpec<Canvas>(::Canvas)
}