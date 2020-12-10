package hep.dataforge.vision.solid

import hep.dataforge.meta.int
import hep.dataforge.meta.set
import hep.dataforge.names.asName
import hep.dataforge.vision.styleSheet
import hep.dataforge.vision.useStyle
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("UNUSED_VARIABLE")
class PropertyTest {
    @Test
    fun testInheritedProperty() {
        var box: Box? = null
        val group = SolidGroup().apply {
            config["test"] = 22
            group {
                box = box(100, 100, 100)
            }
        }
        assertEquals(22, box?.getProperty("test".asName()).int)
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
        assertEquals(22, box?.getProperty("test".asName()).int)
    }

    @Test
    fun testColor() {
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
        assertEquals("#555555", box?.color)
    }

    @Test
    fun testReferenceStyleProperty() {
        var box: SolidReference? = null
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
        assertEquals("#555555", box?.color)
    }
}