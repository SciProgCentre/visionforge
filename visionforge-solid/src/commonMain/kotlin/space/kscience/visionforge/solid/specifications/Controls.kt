package space.kscience.visionforge.solid.specifications

import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec


public class Controls : Scheme() {
    public companion object : SchemeSpec<Controls>(::Controls)
}