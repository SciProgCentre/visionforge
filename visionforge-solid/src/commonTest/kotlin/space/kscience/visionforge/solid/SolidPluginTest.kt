package space.kscience.visionforge.solid

import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.fetch
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.getChild
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
        val visionManager = Global.fetch(Solids).visionManager
        val meta = visionManager.encodeToMeta(vision)

        val reconstructed = visionManager.decodeFromMeta(meta) as SolidGroup

        assertEquals(
            visionManager.encodeToJsonElement(vision.children.getChild("aBox")!!),
            visionManager.encodeToJsonElement(reconstructed.children.getChild("aBox")!!)
        )
    }
}