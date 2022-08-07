package space.kscience.visionforge.gdml

import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.values.asValue
import space.kscience.gdml.GdmlShowCase
import space.kscience.visionforge.Vision
import space.kscience.visionforge.get
import space.kscience.visionforge.getPropertyValue
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.SolidMaterial
import space.kscience.visionforge.solid.material
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GDMLVisionTest {
    private val cubes = GdmlShowCase.cubes().toVision()

    @Test
    fun testCubesStyles(){
        val segment = cubes.children["composite-000.segment-0"] as Solid
        println(segment.getPropertyValue(Vision.STYLE_KEY))
//        println(segment.computePropertyNode(SolidMaterial.MATERIAL_KEY))
//        println(segment.computeProperty(SolidMaterial.MATERIAL_COLOR_KEY))

        println(segment.material?.meta)

        //println(Solids.encodeToString(cubes))
    }


    @Test
    fun testPrototypeProperty() {
        val child = cubes[Name.of("composite-000","segment-0")]
        assertNotNull(child)
        child.setPropertyValue(SolidMaterial.MATERIAL_COLOR_KEY, "red".asValue())
        assertEquals("red", child.getProperty(SolidMaterial.MATERIAL_COLOR_KEY).string)
    }
}