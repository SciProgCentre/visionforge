package hep.dataforge.vision.solid.specifications

import hep.dataforge.meta.Scheme
import hep.dataforge.meta.SchemeSpec

public class Light : Scheme() {
    public companion object : SchemeSpec<Light>(::Light)
}