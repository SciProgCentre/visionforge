package hep.dataforge.vision.solid

import hep.dataforge.context.Global
import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.get
import kotlin.test.Test
import kotlin.test.assertEquals

class SolidPluginTest {
    val vision = SolidGroup {
        box(100,100,100, name = "aBox")

        sphere(100,name = "aSphere"){
            z = 200
        }
    }

    @DFExperimental
    @Test
    fun testPluginConverter(){
        val plugin = Global.plugins.fetch(SolidManager).visionManager
        val meta = plugin.writeVisionToMeta(vision)

        val reconstructed = plugin.buildSpecificVision<SolidGroup>(meta)

        assertEquals(vision["aBox"],reconstructed["aBox"])
    }
}