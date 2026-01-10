package space.kscience.visionforge.meta

import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.request
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.remove
import space.kscience.dataforge.meta.set
import space.kscience.visionforge.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PropertyFlowTest {

    private val manager = Global.request(VisionManager)

    @Test
    fun testChildrenPropertyFlow(): Unit = runBlocking {

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

        launch {
            val changesFlow = child.flowProperty("test", inherited = true).stateIn(this)

            assertEquals(22, child.readProperty("test", true).int)

            delay(10)
            assertEquals(22, changesFlow.value.int)

            parent.properties["test1"] = 88 // another property

            child.properties.remove("test")

            assertEquals(11, child.readProperty("test", true).int)
            delay(10)
            assertEquals(11, changesFlow.value.int)

            parent.properties["test"] = 33
            assertEquals(33, child.readProperty("test", true).int)

            delay(10)
            assertEquals(33, changesFlow.value.int)


            cancel()
        }
    }
}