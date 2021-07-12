package space.kscience.visionforge.gdml

import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.toName
import space.kscience.dataforge.values.asValue
import space.kscience.gdml.GdmlShowCase
import space.kscience.visionforge.setProperty
import space.kscience.visionforge.solid.SolidMaterial
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GDMLVisionTest {

//    @Test
//    fun testCubesStyles(){
//        val cubes = gdml.toVision()
//        val segment = cubes["composite000.segment_0".toName()] as Solid
//        println(segment.styles)
//        println(segment.material)
//    }


    @Test
    fun testPrototypeProperty() {
        val vision = GdmlShowCase.cubes().toVision()
        val child = vision["composite-000.segment-0".toName()]
        assertNotNull(child)
        child.setProperty(SolidMaterial.MATERIAL_COLOR_KEY, "red".asValue())
        assertEquals("red", child.getProperty(SolidMaterial.MATERIAL_COLOR_KEY).string)
    }
}