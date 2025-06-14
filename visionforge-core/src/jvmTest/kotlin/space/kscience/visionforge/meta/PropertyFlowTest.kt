package space.kscience.visionforge.meta

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.request
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.remove
import space.kscience.dataforge.meta.set
import space.kscience.visionforge.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

internal class PropertyFlowTest {

    private val manager = Global.request(VisionManager)

    @Test
    fun testChildrenPropertyFlow() = runTest(timeout = 200.milliseconds) {
        val parent = MutableVisionGroup(manager) {

            properties {
                "test" put 11
            }

            group("child") {
                properties {
                    "test" put 22
                }
            }

        }

        val child = parent.getVision("child") as MutableVisionGroup<*>

        val changesFlow = child.flowProperty("test", inherited = true)


//        child.inheritedEventFlow().filterIsInstance<VisionPropertyChangedEvent>().onEach { event ->
//            println(event)
//            delay(2)
//            println(child.readProperty("test", inherited = true))
//        }.launchIn(this)

        val collectedValues = ArrayList<Int>(5)

        val collectorJob = changesFlow.onEach {
            collectedValues.add(it.int!!)
        }.launchIn(this)


        delay(2)
        assertEquals(22, child.readProperty("test", true).int)
//        assertEquals(1, collectedValues.size)

        parent.properties["test1"] = 88 // another property

        child.properties.remove("test")

        delay(2)

        assertEquals(11, child.readProperty("test", true).int)
  //      assertEquals(2, collectedValues.size)

        parent.properties["test"] = 33
        delay(2)

        assertEquals(33, child.readProperty("test", true).int)
    //    assertEquals(3, collectedValues.size)

        collectorJob.cancel()
        assertEquals(listOf(22, 11, 33), collectedValues)
        println("finished")
    }
}