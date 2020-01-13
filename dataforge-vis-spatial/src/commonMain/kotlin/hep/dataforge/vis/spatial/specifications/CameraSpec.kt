package hep.dataforge.vis.spatial.specifications

import hep.dataforge.meta.*
import kotlin.math.PI

class CameraSpec(override val config: Config) : Specific {
    var fov by int(FIELD_OF_VIEW)
    //var aspect by double(1.0)
    var nearClip by double(NEAR_CLIP)
    var farClip by double(FAR_CLIP)

    var distance by double(INITIAL_DISTANCE)
    var azimuth by double(INITIAL_AZIMUTH)
    var latitude by double(INITIAL_LATITUDE)
    val zenith: Double get() = PI / 2 - latitude

    companion object : Specification<CameraSpec> {
        override fun wrap(config: Config): CameraSpec = CameraSpec(config)
        const val INITIAL_DISTANCE = 300.0
        const val INITIAL_AZIMUTH = 0.0
        const val INITIAL_LATITUDE = PI/6
        const val NEAR_CLIP = 0.1
        const val FAR_CLIP = 10000.0
        const val FIELD_OF_VIEW = 75
    }
}