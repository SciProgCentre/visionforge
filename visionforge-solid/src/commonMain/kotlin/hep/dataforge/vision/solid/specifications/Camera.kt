package hep.dataforge.vision.solid.specifications

import hep.dataforge.meta.Scheme
import hep.dataforge.meta.SchemeSpec
import hep.dataforge.meta.double
import hep.dataforge.meta.int
import kotlin.math.PI

public class Camera : Scheme() {
    public var fov: Int by int(FIELD_OF_VIEW)

    //var aspect by double(1.0)
    public var nearClip: Double by double(NEAR_CLIP)
    public var farClip: Double by double(FAR_CLIP)

    public var distance: Double by double(INITIAL_DISTANCE)
    public var azimuth: Double by double(INITIAL_AZIMUTH)
    public var latitude: Double by double(INITIAL_LATITUDE)
    public val zenith: Double get() = PI / 2 - latitude

    public companion object : SchemeSpec<Camera>(::Camera) {
        public const val INITIAL_DISTANCE: Double = 300.0
        public const val INITIAL_AZIMUTH: Double = 0.0
        public const val INITIAL_LATITUDE: Double = PI / 6
        public const val NEAR_CLIP: Double = 0.1
        public const val FAR_CLIP: Double = 10000.0
        public const val FIELD_OF_VIEW: Int = 75
    }
}