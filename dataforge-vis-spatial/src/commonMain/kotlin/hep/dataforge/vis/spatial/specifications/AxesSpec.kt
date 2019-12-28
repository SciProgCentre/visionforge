package hep.dataforge.vis.spatial.specifications

import hep.dataforge.meta.*

class AxesSpec(override val config: Config) : Specific {
    var visible by boolean(!config.isEmpty())
    var size by double(AXIS_SIZE)
    var width by double(AXIS_WIDTH)

    companion object : Specification<AxesSpec> {
        override fun wrap(config: Config): AxesSpec = AxesSpec(config)

        const val AXIS_SIZE = 1000.0
        const val AXIS_WIDTH = 3.0

    }
}