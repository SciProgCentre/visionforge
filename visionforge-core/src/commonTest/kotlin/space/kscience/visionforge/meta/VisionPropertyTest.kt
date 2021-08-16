package space.kscience.visionforge.meta

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.values.asValue
import space.kscience.visionforge.VisionBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class VisionPropertyTest {
    @Test
    fun testPropertyWrite(){
        val vision = VisionBase()
        vision.meta["fff"] = 2
        vision.meta["fff.ddd"] = false

        assertEquals(2, vision.meta["fff"]?.int)
        assertEquals(false, vision.meta["fff.ddd"]?.boolean)
    }

    @Test
    fun testPropertyEdit(){
        val vision = VisionBase()
        vision.meta.getOrCreate("fff.ddd").apply {
            value = 2.asValue()
        }
        assertEquals(2, vision.meta["fff.ddd"]?.int)
        assertNotEquals(true, vision.meta["fff.ddd"]?.boolean)
    }

    internal class TestScheme: Scheme(){
        var ddd by int()
        companion object: SchemeSpec<TestScheme>(::TestScheme)
    }

    @Test
    fun testPropertyUpdate(){
        val vision = VisionBase()
        vision.meta.getOrCreate("fff").updateWith(TestScheme){
            ddd = 2
        }
        assertEquals(2, vision.meta["fff.ddd"]?.int)
    }
}