package space.kscience.visionforge.solid

import kotlinx.coroutines.ExperimentalCoroutinesApi
import space.kscience.dataforge.meta.getValue
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.set
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.getValue
import space.kscience.visionforge.styleSheet
import space.kscience.visionforge.styles
import space.kscience.visionforge.useStyle
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("UNUSED_VARIABLE")
class SolidPropertyTest {
    @Test
    fun testColor() {
        val box = Box(10.0f, 10.0f, 10.0f)
        box.material {
            //meta["color"] = "pink"
            color("pink")
        }
        assertEquals("pink", box.properties.getValue("material.color")?.string)
        assertEquals("pink", box.color.string)
    }

    @Test
    fun testColorUpdate() {
        val box = Box(10.0f, 10.0f, 10.0f)

        box.material {
            color("pink")
        }

        assertEquals("pink", box.properties[SolidMaterial.MATERIAL_COLOR_KEY].string)
    }

    @Test
    fun testInheritedProperty() {
        var box: Box? = null
        val group = SolidGroup().apply {
            properties["test"] = 22
            solidGroup {
                box = box(100, 100, 100)
            }
        }
        assertEquals(22, box?.properties?.getValue("test", inherit = true)?.int)
    }

    @Test
    fun testStyleProperty() {
        var box: Box? = null
        val group = testSolids.solidGroup {
            styleSheet {
                update("testStyle") {
                    "test" put 22
                }
            }
            solidGroup {
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
            solidGroup {
                box = box(100, 100, 100) {
                    useStyle("testStyle")
                }
            }
        }
        assertEquals("#555555", box?.color?.string)
    }

    @Test
    fun testReferenceStyleProperty() {
        var box: SolidReference? = null
        val group = testSolids.solidGroup {
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
            solidGroup {
                box = ref("box".asName())
            }
        }
        assertEquals("#555555", box!!.color.string)
    }
}