package space.kscience.visionforge.solid

import space.kscience.dataforge.meta.descriptors.validate
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import kotlin.test.Test

class SpecTest {

    @Test
    fun testCanvasSpec() {
        val spec = Canvas3DOptions {
            camera.distance = 1200.0
            layers = listOf(0, 1, 2, 3, 4, 5, 6)
        }

        Canvas3DOptions.descriptor.validate(spec.meta)
    }
}