package hep.dataforge.vision.solid

import hep.dataforge.vision.get
import hep.dataforge.vision.style
import hep.dataforge.vision.useStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class SolidReferenceTest {
    val groupWithReference = SolidGroup {
        val theStyle by style {
            SolidMaterial.MATERIAL_COLOR_KEY put "red"
        }
        ref("test", Box(100f,100f,100f).apply {
            color("blue")
            useStyle(theStyle)
        })
    }


    @Test
    fun testReferenceProperty(){
        assertEquals("blue", (groupWithReference["test"] as Solid).color.string)
    }
}