package hep.dataforge.vision.solid

import hep.dataforge.context.Global
import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.get
import kotlin.test.Test
import kotlin.test.assertEquals

class SolidPluginTest {
    val vision = SolidGroup {
        box(100, 100, 100, name = "aBox")

        sphere(100, name = "aSphere") {
            z = 200
        }
    }

    @DFExperimental
    @Test
    fun testPluginConverter() {
        val visionManager = Global.plugins.fetch(SolidManager).visionManager
        val meta = visionManager.encodeToMeta(vision)

        val reconstructed = visionManager.decodeFromMeta(meta) as SolidGroup

        assertEquals(visionManager.encodeToJsonElement(vision["aBox"]!!), visionManager.encodeToJsonElement(reconstructed["aBox"]!!))
    }
}