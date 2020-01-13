package hep.dataforge.vis.spatial.specifications

import hep.dataforge.meta.Config
import hep.dataforge.meta.Specific
import hep.dataforge.meta.Specification

class ControlsSpec(override val config: Config) : Specific {
    companion object : Specification<ControlsSpec> {
        override fun wrap(config: Config): ControlsSpec = ControlsSpec(config)
    }
}