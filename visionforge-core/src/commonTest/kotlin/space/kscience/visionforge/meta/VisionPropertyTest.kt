package space.kscience.visionforge.meta

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.request
import space.kscience.dataforge.meta.*
import space.kscience.visionforge.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.milliseconds


private class TestScheme : Scheme() {
    var ddd by int()

    companion object : SchemeSpec<TestScheme>(::TestScheme)
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class VisionPropertyTest {

    private val manager = Global.request(VisionManager)

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
    fun testChildrenPropertyPropagation() = runTest(timeout = 200.milliseconds) {
        val group = Global.request(VisionManager).group {
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

        val deferred: CompletableDeferred<Value?> = CompletableDeferred()

        var callCounter = 0

        val subscription = child.useProperty("test", inherit = true) {
            deferred.complete(it.value)
            callCounter++
        }

        assertEquals(22, deferred.await()?.int)
        assertEquals(1, callCounter)

        child.properties.remove("test")

        assertEquals(11, child.properties.getProperty("test", inherit = true).int)
//        assertEquals(11, deferred.await()?.int)
//        assertEquals(2, callCounter)
        subscription.cancel()
    }

    @Test
    fun testChildrenPropertyFlow() = runTest(timeout = 200.milliseconds) {
        val group = Global.request(VisionManager).group {

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
            val list = child.flowPropertyValue("test", inherit = true).take(3).map { it?.int }.toList()
            assertEquals(22, list.first())
            //assertEquals(11, list[1]) //a race
            assertEquals(33, list.last())
        }

        //wait for subscription to be created
        delay(5)

        child.properties.remove("test")
        group.properties["test"] = 33
    }
}