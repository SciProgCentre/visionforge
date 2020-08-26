package hep.dataforge.vision.gdml

import hep.dataforge.meta.setItem
import hep.dataforge.meta.string
import hep.dataforge.names.toName
import hep.dataforge.values.asValue
import hep.dataforge.vision.gdml.demo.cubes
import hep.dataforge.vision.solid.SolidMaterial
import kotlin.test.Test
import kotlin.test.assertEquals

class GDMLVisualTest {
    val gdml = cubes()

//    @Test
//    fun testCubesStyles(){
//        val cubes = gdml.toVision()
//        val segment = cubes["composite000.segment_0".toName()] as Solid
//        println(segment.styles)
//        println(segment.material)
//    }


    @Test
    fun testPrototypeProperty() {
        val visual = gdml.toVision()
        visual["composite000.segment_0".toName()]?.setItem(SolidMaterial.MATERIAL_COLOR_KEY, "red".asValue())
        assertEquals("red", visual["composite000.segment_0".toName()]?.getItem(SolidMaterial.MATERIAL_COLOR_KEY).string)
    }
}