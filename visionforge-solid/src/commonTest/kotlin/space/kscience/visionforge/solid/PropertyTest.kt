package space.kscience.visionforge.solid

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.values.int
import space.kscience.visionforge.*
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("UNUSED_VARIABLE")
class PropertyTest {
    @Test
    fun testColorUpdate(){
        val box = Box(10.0f, 10.0f,10.0f)
        box.material {
            //meta["color"] = "pink"
            color("pink")
        }
        assertEquals("pink", box.meta["material.color"]?.string)
    }

    @Test
    fun testInheritedProperty() {
        var box: Box? = null
        val group = SolidGroup().apply {
            setPropertyNode("test", 22)
            group {
                box = box(100, 100, 100)
            }
        }
        assertEquals(22, box?.getPropertyValue("test", inherit = true)?.int)
    }

    @Test
    fun testStyleProperty() {
        var box: Box? = null
        val group = SolidGroup().apply {
            styleSheet {
                set("testStyle") {
                    "test" put 22
                }
            }
            group {
                box = box(100, 100, 100).apply {
                    useStyle("testStyle")
                }
            }
        }
        assertEquals(22, box?.getPropertyValue("test")?.int)
    }

    @Test
    fun testStyleColor() {
        var box: Box? = null
        val group = SolidGroup().apply {
            styleSheet {
                set("testStyle") {
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
        var box: SolidReferenceGroup? = null
        val group = SolidGroup{
            styleSheet {
                set("testStyle") {
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
        assertEquals("#555555", box?.color.string)
    }
}