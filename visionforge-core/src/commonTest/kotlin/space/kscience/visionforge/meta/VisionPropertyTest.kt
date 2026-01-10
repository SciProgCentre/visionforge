package space.kscience.visionforge.meta

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.request
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.get
import space.kscience.dataforge.names.parseAsName
import space.kscience.visionforge.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds


private class TestScheme : Scheme() {
    var ddd by int()

    companion object : SchemeSpec<TestScheme>(::TestScheme)
}

internal class VisionPropertyTest {

    private val manager = Global.request(VisionManager)

    @Test
    fun testPropertyWrite() {
        val vision = SimpleVisionGroup()
        vision.properties["fff"] = 2
        vision.properties["fff.ddd"] = false

        assertEquals(2, vision.readProperty("fff")?.int)
        assertEquals(false, vision.readProperty("fff.ddd")?.boolean)
    }

    @Test
    fun testPropertyEdit() {
        val vision = SimpleVisionGroup()
        vision.mutableProperty("fff.ddd".parseAsName()).apply {
            value = 2.asValue()
        }
        assertEquals(2, vision.readProperty("fff.ddd")?.int)
        assertEquals(true, vision.readProperty("fff.ddd")?.boolean)
    }

    @Test
    fun testPropertyUpdate() {
        val vision = SimpleVisionGroup()
        vision.mutableProperty("fff".asName()).updateWith(TestScheme) {
            ddd = 2
        }
        assertEquals(2, vision.readProperty("fff.ddd")?.int)
    }

    @Test
    fun testChildrenPropertyPropagation() = runTest(timeout = 200.milliseconds) {
        val group = VisionGroup(Global.visionManager) {
            properties {
                "test" put 11
            }
            group("child") {
                properties {
                    "test" put 22
                }
            }
        }

        val child = group.visions["child"] as MutableVision

        val deferred: CompletableDeferred<Value?> = CompletableDeferred()

        var callCounter = 0

        val subscription = child.useProperty("test", inherited = true) {
            deferred.complete(it?.value)
            callCounter++
        }

        assertEquals(22, deferred.await()?.int)
        assertEquals(1, callCounter)

        child.properties.remove("test")

        assertEquals(11, child.readProperty("test", inherited = true).int)
//        assertEquals(11, deferred.await()?.int)
//        assertEquals(2, callCounter)
        subscription.cancel()
    }

}