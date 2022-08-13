package space.kscience.visionforge.meta

import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.fetch
import space.kscience.dataforge.meta.*
import space.kscience.visionforge.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


private class TestScheme : Scheme() {
    var ddd by int()

    companion object : SchemeSpec<TestScheme>(::TestScheme)
}

internal class VisionPropertyTest {

    private val manager = Global.fetch(VisionManager)

    @Test
    fun testPropertyWrite() {
        val vision = manager.group()
        vision.properties["fff"] = 2
        vision.properties["fff.ddd"] = false

        assertEquals(2, vision.properties.getValue("fff")?.int)
        assertEquals(false, vision.properties.getValue("fff.ddd")?.boolean)
    }

    @Test
    fun testPropertyEdit() {
        val vision = manager.group()
        vision.properties.getProperty("fff.ddd").apply {
            value = 2.asValue()
        }
        assertEquals(2, vision.properties.getValue("fff.ddd")?.int)
        assertNotEquals(true, vision.properties.getValue("fff.ddd")?.boolean)
    }

    @Test
    fun testPropertyUpdate() {
        val vision = manager.group()
        vision.properties.getProperty("fff").updateWith(TestScheme) {
            ddd = 2
        }
        assertEquals(2, vision.properties.getValue("fff.ddd")?.int)
    }

    @Test
    fun testChildrenPropertyPropagation() = runTest(dispatchTimeoutMs = 200) {
        val group = Global.fetch(VisionManager).group {
            properties {
                "test" put 11
            }
            group("child") {
                properties {
                    "test" put 22
                }
            }
        }

        val child = group.children["child"]!!

        var value: Value? = null

        var callCounter = 0

        child.useProperty("test", inherit = true) {
            callCounter++
            value = it.value
        }

        assertEquals(22, value?.int)
        assertEquals(1, callCounter)

        child.properties.remove("test")

        //Need this to avoid the race
        delay(20)

        assertEquals(11, child.properties.getProperty("test", inherit = true).int)
        assertEquals(11, value?.int)
        assertEquals(2, callCounter)
    }

    @Test
    fun testChildrenPropertyFlow() = runTest(dispatchTimeoutMs = 200) {
        val group = Global.fetch(VisionManager).group {
            properties {
                "test" put 11
            }
            group("child") {
                properties {
                    "test" put 22
                }
            }
        }

        val child = group.children["child"]!!

        launch {
            child.flowPropertyValue("test", inherit = true).collectIndexed { index, value ->
                if (index == 0) {
                    assertEquals(22, value?.int)
                } else if (index == 1) {
                    assertEquals(11, value?.int)
                    cancel()
                }
            }
        }
        //wait for subscription to be created
        delay(10)

        child.properties.remove("test")
    }
}