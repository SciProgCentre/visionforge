package hep.dataforge.vision.spatial.gdml

import hep.dataforge.meta.setItem
import hep.dataforge.meta.string
import hep.dataforge.names.toName
import hep.dataforge.values.asValue
import hep.dataforge.vision.spatial.Material3D
import hep.dataforge.vision.spatial.gdml.demo.cubes
import kotlin.test.Test
import kotlin.test.assertEquals

class GDMLVisualTest {
    @Test
    fun testPrototypeProperty() {
        val gdml = cubes()
        val visual = gdml.toVisual()
        visual["composite000.segment0".toName()]?.setItem(Material3D.MATERIAL_COLOR_KEY, "red".asValue())
        assertEquals("red", visual["composite000.segment0".toName()]?.getItem(Material3D.MATERIAL_COLOR_KEY).string)
    }
}