package hep.dataforge.vision.solid.specifications

import hep.dataforge.meta.Scheme
import hep.dataforge.meta.SchemeSpec
import hep.dataforge.meta.boolean
import hep.dataforge.meta.double

public class Axes : Scheme() {
    public var visible: Boolean by boolean(false)
    public var size: Double by double(AXIS_SIZE)
    public var width: Double by double(AXIS_WIDTH)

    public companion object : SchemeSpec<Axes>(::Axes) {
        public const val AXIS_SIZE: Double = 1000.0
        public const val AXIS_WIDTH: Double = 3.0
    }
}