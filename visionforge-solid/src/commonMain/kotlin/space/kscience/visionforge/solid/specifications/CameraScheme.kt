package space.kscience.visionforge.solid.specifications

import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.meta.double
import space.kscience.dataforge.meta.int
import kotlin.math.PI

public class CameraScheme : Scheme() {
    public var fov: Int by int(FIELD_OF_VIEW)

    //var aspect by double(1.0)
    public var nearClip: Double by double(NEAR_CLIP)
    public var farClip: Double by double(FAR_CLIP)

    public var distance: Double by double(INITIAL_DISTANCE)
    public var azimuth: Double by double(INITIAL_AZIMUTH)
    public var latitude: Double by double(INITIAL_LATITUDE)

    public companion object : SchemeSpec<CameraScheme>(::CameraScheme) {
        public const val INITIAL_DISTANCE: Double = 300.0
        public const val INITIAL_AZIMUTH: Double = 0.0
        public const val INITIAL_LATITUDE: Double = PI / 6
        public const val NEAR_CLIP: Double = 0.1
        public const val FAR_CLIP: Double = 10000.0
        public const val FIELD_OF_VIEW: Int = 75

        override val descriptor: MetaDescriptor by lazy {
            MetaDescriptor {
                value(CameraScheme::fov) {
                    default(FIELD_OF_VIEW)
                }
                value(CameraScheme::nearClip) {
                    default(NEAR_CLIP)
                }
                value(CameraScheme::farClip) {
                    default(FAR_CLIP)
                }
                value(CameraScheme::distance) {
                    default(INITIAL_DISTANCE)
                }
                value(CameraScheme::azimuth) {
                    default(INITIAL_AZIMUTH)
                }
                value(CameraScheme::latitude) {
                    default(INITIAL_LATITUDE)
                }
            }
        }
    }
}

public val CameraScheme.zenith: Double get() = PI / 2 - latitude