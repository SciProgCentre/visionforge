package hep.dataforge.vis.spatial.specifications

import hep.dataforge.meta.*

class Axes : Scheme() {
    var visible by boolean(!config.isEmpty())
    var size by double(AXIS_SIZE)
    var width by double(AXIS_WIDTH)

    companion object : SchemeSpec<Axes>(::Axes) {
        const val AXIS_SIZE = 1000.0
        const val AXIS_WIDTH = 3.0
    }
}