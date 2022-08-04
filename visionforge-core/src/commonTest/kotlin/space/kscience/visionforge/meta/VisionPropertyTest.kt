package space.kscience.visionforge.meta

import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.updateWith
import space.kscience.dataforge.values.asValue
import space.kscience.dataforge.values.boolean
import space.kscience.dataforge.values.int
import space.kscience.visionforge.VisionGroup
import space.kscience.visionforge.getProperty
import space.kscience.visionforge.getPropertyValue
import space.kscience.visionforge.setPropertyValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class VisionPropertyTest {
    @Test
    fun testPropertyWrite(){
        val vision = VisionGroup()
        vision.setPropertyValue("fff", 2)
        vision.setPropertyValue("fff.ddd", false)

        assertEquals(2, vision.getPropertyValue("fff")?.int)
        assertEquals(false, vision.getPropertyValue("fff.ddd")?.boolean)
    }

    @Test
    fun testPropertyEdit(){
        val vision = VisionGroup()
        vision.getProperty("fff.ddd").apply {
            value = 2.asValue()
        }
        assertEquals(2, vision.getPropertyValue("fff.ddd")?.int)
        assertNotEquals(true, vision.getPropertyValue("fff.ddd")?.boolean)
    }

    internal class TestScheme: Scheme(){
        var ddd by int()
        companion object: SchemeSpec<TestScheme>(::TestScheme)
    }

    @Test
    fun testPropertyUpdate(){
        val vision = VisionGroup()
        vision.getProperty("fff").updateWith(TestScheme){
            ddd = 2
        }
        assertEquals(2, vision.getPropertyValue("fff.ddd")?.int)
    }
}