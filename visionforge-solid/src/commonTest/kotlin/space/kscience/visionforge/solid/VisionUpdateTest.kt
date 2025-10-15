package space.kscience.visionforge.solid

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.VisionChange
import space.kscience.visionforge.getOrCreateChange
import space.kscience.visionforge.useProperty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

internal class VisionUpdateTest {

    @Test
    fun testVisionUpdate() = runTest {
        val targetVision = testSolids.solidGroup {
            box(200, 200, 200, name = "origin")
        }
        val dif = testVisionManager.VisionChange {
            solidGroup("top") {
                color(123)
                box(100, 100, 100)
            }

            getOrCreateChange("top".asName()).propertyChanged(
                SolidMaterial.MATERIAL_COLOR_KEY,
                Meta("red")
            )
            getOrCreateChange("origin".asName()).propertyChanged(
                SolidMaterial.MATERIAL_COLOR_KEY,
                Meta("red")
            )
        }
        targetVision.receiveEvent(dif)
        assertTrue { targetVision["top"] is SolidGroup }
        assertEquals("red", (targetVision["origin"] as Solid).color.string) // Should work
        assertEquals(
            "#00007b",
            (targetVision["top"] as Solid).color.string
        ) // new item always takes precedence
    }

    @Test
    fun testVisionChangeSerialization() {
        val change = testVisionManager.VisionChange {
            solidGroup("top") {
                color(123)
                box(100, 100, 100)
            }
            getOrCreateChange("top".asName()).propertyChanged(
                SolidMaterial.MATERIAL_COLOR_KEY,
                Meta("red")
            )
            getOrCreateChange("origin".asName()).propertyChanged(
                SolidMaterial.MATERIAL_COLOR_KEY,
                Meta("red")
            )
        }
        val serialized = testVisionManager.jsonFormat.encodeToString(VisionChange.serializer(), change)
        println(serialized)
        val reconstructed = testVisionManager.jsonFormat.decodeFromString(VisionChange.serializer(), serialized)
        assertEquals(change.properties, reconstructed.properties)
    }

    @Test
    fun useProperty() = runTest(timeout = 1.seconds) {

        val group = testSolids.solidGroup {
            box(100, 100, 100)
        }

        val box = group.visions.values.first()

        val collected = Channel<String?>(5)

        box.useProperty(
            propertyName = SolidMaterial.MATERIAL_COLOR_KEY,
            scope = backgroundScope
        ) {
            println(it.string)
            collected.send(it.string)
        }

        delay(1)

        group.color("red")
        group.color("green")
        box.color("blue")
        delay(1)

        assertEquals("blue", box.readProperty(SolidMaterial.MATERIAL_COLOR_KEY).string)
        assertEquals("blue", box.color.string)

        val list = collected.consumeAsFlow().take(4).toList()

        assertEquals(null, list.first())
        assertEquals("blue", list.last())

    }
}