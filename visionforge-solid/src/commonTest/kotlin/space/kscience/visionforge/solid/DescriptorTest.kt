package space.kscience.visionforge.solid

import space.kscience.dataforge.meta.ValueType
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DescriptorTest {
    @Test
    fun canvasDescriptor() {
        val descriptor = Canvas3DOptions.descriptor
        //println(descriptor.config)
        val axesSize = descriptor["axes.size"]
        assertNotNull(axesSize)
        assertTrue {
            ValueType.NUMBER in axesSize.valueTypes!!
        }
    }
}