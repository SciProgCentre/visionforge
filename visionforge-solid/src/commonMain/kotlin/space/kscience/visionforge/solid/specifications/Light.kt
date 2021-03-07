package space.kscience.visionforge.solid.specifications

import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec

public class Light : Scheme() {
    public companion object : SchemeSpec<Light>(::Light)
}