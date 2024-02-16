package space.kscience.visionforge.meta

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Timeout
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.request
import space.kscience.dataforge.meta.*
import space.kscience.visionforge.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PropertyFlowTest {

    private val manager = Global.request(VisionManager)

    @Test
    @Timeout(200)
    fun testChildrenPropertyFlow() = runBlocking{
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

        val changesFlow = child.flowPropertyValue("test", inherit = true).map {
            it!!.int
        }

        val collectedValues = ArrayList<Int>(5)

        val collectorJob = changesFlow.onEach {
            collectedValues.add(it)
        }.launchIn(this)


        delay(2)
        assertEquals(22, child.properties["test", true].int)

        child.properties.remove("test")
        delay(2)

        assertEquals(11, child.properties["test", true].int)
        group.properties["test"] = 33
        delay(2)

        assertEquals(33, child.properties["test", true].int)

        collectorJob.cancel()
        assertEquals(listOf(22, 11, 33), collectedValues)
    }
}