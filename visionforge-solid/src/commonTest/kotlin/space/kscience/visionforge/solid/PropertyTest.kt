package space.kscience.visionforge.solid

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.*
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("UNUSED_VARIABLE")
class PropertyTest {
    @Test
    fun testColor() {
        val box = Box(10.0f, 10.0f, 10.0f)
        box.material {
            //meta["color"] = "pink"
            color.set("pink")
        }
        assertEquals("pink", box.properties.getValue("material.color")?.string)
        assertEquals("pink", box.color.string)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testColorUpdate() = runTest {
        val box = Box(10.0f, 10.0f, 10.0f)

        val c = CompletableDeferred<String?>()


        box.onPropertyChange {
            if (it == SolidMaterial.MATERIAL_COLOR_KEY) {
                c.complete(box.color.string)
            }
        }

        box.material {
            color.set("pink")
        }

        assertEquals("pink", withTimeout(50) { c.await() })

    }

    @Test
    fun testInheritedProperty() {
        var box: Box? = null
        val group = SolidGroup().apply {
            properties["test"] = 22
            group {
                box = box(100, 100, 100)
            }
        }
        assertEquals(22, box?.properties?.getValue("test", inherit = true)?.int)
    }

    @Test
    fun testStyleProperty() {
        var box: Box? = null
        val group = SolidGroup {
            styleSheet {
                update("testStyle") {
                    "test" put 22
                }
            }
            group {
                box = box(100, 100, 100) {
                    useStyle("testStyle")
                }
            }
        }
        assertEquals(22, box?.properties?.getValue("test")?.int)
    }

    @Test
    fun testStyleColor() {
        var box: Box? = null
        val group = SolidGroup().apply {
            styleSheet {
                update("testStyle") {
                    SolidMaterial.MATERIAL_COLOR_KEY put "#555555"
                }
            }
            group {
                box = box(100, 100, 100) {
                    useStyle("testStyle")
                }
            }
        }
        assertEquals("#555555", box?.color.string)
    }

    @Test
    fun testReferenceStyleProperty() {
        var box: SolidReference? = null
        val group = SolidGroup {
            styleSheet {
                update("testStyle") {
                    SolidMaterial.MATERIAL_COLOR_KEY put "#555555"
                }
            }
            prototypes {
                box(100, 100, 100, name = "box") {
                    styles = listOf("testStyle")
                }
            }
            group {
                box = ref("box".asName())
            }
        }
        assertEquals("#555555", box!!.color.string)
    }
}