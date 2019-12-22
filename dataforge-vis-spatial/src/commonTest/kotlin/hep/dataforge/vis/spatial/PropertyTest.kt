package hep.dataforge.vis.spatial

import hep.dataforge.meta.int
import hep.dataforge.meta.set
import hep.dataforge.names.asName
import hep.dataforge.vis.common.updateStyle
import hep.dataforge.vis.common.useStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class PropertyTest {
    @Test
    fun testInheritedProperty(){
        var box: Box? = null
        val group = VisualGroup3D().apply {
            config["test"] = 22
            group {
                box = box(100,100,100)
            }
        }
        assertEquals(22, box?.getProperty("test".asName()).int)
    }

    @Test
    fun testStyleProperty(){
        var box: Box? = null
        val group = VisualGroup3D().apply {
            updateStyle("testStyle"){
                "test" put 22
            }
            group {
                box = box(100,100,100).apply {
                    useStyle("testStyle")
                }
            }
        }
        assertEquals(22, box?.getProperty("test".asName()).int)
    }

    @Test
    fun testColor(){
        var box: Box? = null
        val group = VisualGroup3D().apply {
            updateStyle("testStyle"){
                Material3D.MATERIAL_COLOR_KEY put "#555555"
            }
            group {
                box = box(100,100,100){
                    useStyle("testStyle")
                }
            }
        }
        assertEquals("#555555", box?.color)
    }
}