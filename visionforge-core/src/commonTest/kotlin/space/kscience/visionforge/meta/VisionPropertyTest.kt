package space.kscience.visionforge.meta

import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.updateWith
import space.kscience.dataforge.values.asValue
import space.kscience.dataforge.values.boolean
import space.kscience.dataforge.values.int
import space.kscience.visionforge.VisionGroup
import space.kscience.visionforge.get
import space.kscience.visionforge.getValue
import space.kscience.visionforge.set
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


private class TestScheme : Scheme() {
    var ddd by int()

    companion object : SchemeSpec<TestScheme>(::TestScheme)
}

internal class VisionPropertyTest {
    @Test
    fun testPropertyWrite() {
        val vision = VisionGroup()
        vision.properties["fff"] = 2
        vision.properties["fff.ddd"] = false

        assertEquals(2, vision.properties.getValue("fff")?.int)
        assertEquals(false, vision.properties.getValue("fff.ddd")?.boolean)
    }

    @Test
    fun testPropertyEdit() {
        val vision = VisionGroup()
        vision.properties["fff.ddd"].apply {
            value = 2.asValue()
        }
        assertEquals(2, vision.properties.getValue("fff.ddd")?.int)
        assertNotEquals(true, vision.properties.getValue("fff.ddd")?.boolean)
    }

    @Test
    fun testPropertyUpdate() {
        val vision = VisionGroup()
        vision.properties["fff"].updateWith(TestScheme) {
            ddd = 2
        }
        assertEquals(2, vision.properties.getValue("fff.ddd")?.int)
    }
}