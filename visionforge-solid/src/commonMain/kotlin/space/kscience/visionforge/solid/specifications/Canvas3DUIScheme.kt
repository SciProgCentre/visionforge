package space.kscience.visionforge.solid.specifications

import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.boolean
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.ValueRestriction


public class Canvas3DUIScheme : Scheme() {

    public var enabled: Boolean by boolean { true }

    public companion object : SchemeSpec<Canvas3DUIScheme>(::Canvas3DUIScheme) {
        override val descriptor: MetaDescriptor = MetaDescriptor {
            valueRestriction = ValueRestriction.ABSENT
        }
    }
}