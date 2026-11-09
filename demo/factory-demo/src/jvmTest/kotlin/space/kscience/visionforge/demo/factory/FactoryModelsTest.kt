package space.kscience.visionforge.demo.factory

import space.kscience.visionforge.demo.factory.model.FactorySceneConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FactoryModelsTest {

    @Test
    fun testLoadSceneConfig() {
        val config = FactorySceneConfig.loadFromResources()
        assertEquals("nmz-mog-3d/1.0", config.schema)
        assertEquals(3, config.layout.lines.size)
        assertEquals(42.0, config.layout.lineSpacing_m)
        assertEquals(26, config.equipment.size)
        assertEquals(26, config.streams.size)
        assertTrue(config.materials.containsKey("Concrete"))
        assertTrue(config.materials.containsKey("FRP"))
        assertTrue(config.materials.containsKey("LinedSteel"))
        assertTrue(config.gost14202.containsKey("1"))
        assertTrue(config.states.containsKey("run"))
        assertTrue(config.states.containsKey("flush"))
    }
}
