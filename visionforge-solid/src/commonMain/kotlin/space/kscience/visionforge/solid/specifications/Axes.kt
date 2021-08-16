package space.kscience.visionforge.solid.specifications

import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.boolean
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.meta.double

public class Axes : Scheme() {
    public var visible: Boolean by boolean(false)
    public var size: Double by double(AXIS_SIZE)
    public var width: Double by double(AXIS_WIDTH)

    public companion object : SchemeSpec<Axes>(::Axes) {
        public const val AXIS_SIZE: Double = 1000.0
        public const val AXIS_WIDTH: Double = 3.0

        override val descriptor: MetaDescriptor by lazy {
            MetaDescriptor {
                value(Axes::visible){
                    default(false)
                }
                value(Axes::size){
                    default(AXIS_SIZE)
                }
                value(Axes::width){
                    default(AXIS_WIDTH)
                }
            }
        }
    }
}