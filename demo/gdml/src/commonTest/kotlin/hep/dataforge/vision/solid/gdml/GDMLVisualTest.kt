package hep.dataforge.vision.solid.gdml

import hep.dataforge.meta.setItem
import hep.dataforge.meta.string
import hep.dataforge.names.toName
import hep.dataforge.values.asValue
import hep.dataforge.vision.solid.SolidMaterial
import hep.dataforge.vision.solid.gdml.demo.cubes
import kotlin.test.Test
import kotlin.test.assertEquals

class GDMLVisualTest {
    @Test
    fun testPrototypeProperty() {
        val gdml = cubes()
        val visual = gdml.toVision()
        visual["composite000.segment0".toName()]?.setItem(SolidMaterial.MATERIAL_COLOR_KEY, "red".asValue())
        assertEquals("red", visual["composite000.segment0".toName()]?.getItem(SolidMaterial.MATERIAL_COLOR_KEY).string)
    }
}