package space.kscience.visionforge.solid

import kotlinx.coroutines.ExperimentalCoroutinesApi
import space.kscience.dataforge.meta.getValue
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.set
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.readProperty
import space.kscience.visionforge.styles
import space.kscience.visionforge.updateStyle
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
        assertEquals(22, box?.readProperty(Name.of("test"), inherited = true)?.int)
    }

    @Test
    fun testStyleProperty() {
        var box: Box? = null
        val group = testSolids.solidGroup {
            updateStyle("testStyle") {
                "test" put 22
            }
            solidGroup {
                box = box(100, 100, 100) {
                    useStyle("testStyle")
                }
            }
        }
        assertEquals(22, box?.readProperty("test")?.int)
    }

    @Test
    fun testStyleColor() {
        var box: Box? = null
        val group = SolidGroup().apply {
            updateStyle("testStyle") {
                SolidMaterial.MATERIAL_COLOR_KEY put "#555555"
                SolidMaterial.MATERIAL_OPACITY_KEY put 0.3
            }
            solidGroup {
                box = box(100, 100, 100) {
                    useStyle("testStyle")
                }
            }
        }
        assertEquals("#555555", box?.readProperty(SolidMaterial.MATERIAL_COLOR_KEY)?.string)
        assertEquals("#555555", box?.color?.string)
        assertEquals(0.3, box?.opacity)
    }

    @Test
    fun testReferenceStyleProperty() {
        var box: SolidReference? = null
        val group = testSolids.solidGroup {
            updateStyle("testStyle") {
                SolidMaterial.MATERIAL_COLOR_KEY put "#555555"
                SolidMaterial.MATERIAL_OPACITY_KEY put 0.3
            }
            prototypes {
                box(100, 100, 100, name = "box") {
                    styles = listOf("testStyle")
                }
            }
            solidGroup {
                box = ref(Name.of("box"))
            }
        }
        assertEquals("#555555", box?.readProperty(SolidMaterial.MATERIAL_COLOR_KEY)?.string)
        assertEquals("#555555", box!!.color.string)
        assertEquals(0.3, box.opacity)
    }
}