package hep.dataforge.vis.spatial.specifications

import hep.dataforge.meta.*

class CanvasSpec(override val config: Config) : Specific {
    var axes by spec(AxesSpec)
    var camera by spec(CameraSpec)
    var controls by spec(ControlsSpec)
    var minSize by int(300)

    companion object: Specification<CanvasSpec>{
        override fun wrap(config: Config): CanvasSpec = CanvasSpec(config)

    }
}