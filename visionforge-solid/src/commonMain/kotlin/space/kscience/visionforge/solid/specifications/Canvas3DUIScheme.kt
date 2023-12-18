package space.kscience.visionforge.solid.specifications

import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.boolean


public class Canvas3DUIScheme : Scheme() {

    public var enabled: Boolean by boolean{true}

    public companion object : SchemeSpec<Canvas3DUIScheme>(::Canvas3DUIScheme)
}