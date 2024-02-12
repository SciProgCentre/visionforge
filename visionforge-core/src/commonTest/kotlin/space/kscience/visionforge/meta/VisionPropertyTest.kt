package space.kscience.visionforge.meta

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Semaphore
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
        vision.properties["fff.ddd"].apply {
            value = 2.asValue()
        }
        assertEquals(2, vision.properties.getValue("fff.ddd")?.int)
        assertNotEquals(true, vision.properties.getValue("fff.ddd")?.boolean)
    }

    @Test
    fun testPropertyUpdate() {
        val vision = manager.group()
        vision.properties["fff"].updateWith(TestScheme) {
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

        assertEquals(11, child.properties.get("test", inherit = true).int)
//        assertEquals(11, deferred.await()?.int)
//        assertEquals(2, callCounter)
        subscription.cancel()
    }

    @Test
    fun testChildrenPropertyFlow() = runTest(timeout = 500.milliseconds) {
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

        val semaphore = Semaphore(1, 1)

        val changesFlow = child.flowPropertyValue("test", inherit = true).map {
            semaphore.release()
            it!!.int
        }

        val collectedValues = ArrayList<Int>(5)

        val collectorJob = changesFlow.onEach {
            collectedValues.add(it)
        }.launchIn(this)

        assertEquals(22, child.properties["test", true].int)

        semaphore.acquire()
        child.properties.remove("test")

        assertEquals(11, child.properties["test", true].int)

        semaphore.acquire()
        group.properties["test"] = 33
        assertEquals(33, child.properties["test", true].int)

        semaphore.acquire()
        collectorJob.cancel()
        assertEquals(listOf(22, 11, 33), collectedValues)
    }
}