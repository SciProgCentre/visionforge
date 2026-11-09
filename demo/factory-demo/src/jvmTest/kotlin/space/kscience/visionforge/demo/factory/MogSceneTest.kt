package space.kscience.visionforge.demo.factory

import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.parseAsName
import space.kscience.visionforge.demo.factory.model.FactorySceneConfig
import space.kscience.visionforge.demo.factory.scene.MogEquipmentBuilder
import space.kscience.visionforge.demo.factory.scene.MogSceneBuilder
import space.kscience.visionforge.solid.SolidGroup
import space.kscience.visionforge.solid.getVision
import space.kscience.visionforge.styles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MogSceneTest {

    @Test
    fun testNozzleCoordinatesAgainstReport() {
        val config = FactorySceneConfig.loadFromResources()
        val nozzlesByEq = config.equipment.associate { eq ->
            eq.id to MogEquipmentBuilder.calculateNozzles(eq)
        }
        val nozzlesMap = nozzlesByEq.values.flatten().associateBy { "${it.equipmentId}.${it.spec.id}" }

        // Test B-01.GasIn
        val b01GasIn = nozzlesMap["B-01.GasIn"]
        assertNotNull(b01GasIn)
        assertEquals(8.0f, b01GasIn.connWorld.x, 0.01f)
        assertEquals(0.0f, b01GasIn.connWorld.y, 0.01f)
        assertEquals(25.6f, b01GasIn.connWorld.z, 0.01f)

        // Test B-01.GasOut
        val b01GasOut = nozzlesMap["B-01.GasOut"]
        assertNotNull(b01GasOut)
        assertEquals(10.9f, b01GasOut.connWorld.x, 0.01f)
        assertEquals(0.0f, b01GasOut.connWorld.y, 0.01f)
        assertEquals(8.5f, b01GasOut.connWorld.z, 0.01f)

        // Test B-02.GasIn
        val b02GasIn = nozzlesMap["B-02.GasIn"]
        assertNotNull(b02GasIn)
        assertEquals(18.6f, b02GasIn.connWorld.x, 0.01f)
        assertEquals(0.0f, b02GasIn.connWorld.y, 0.01f)
        assertEquals(8.5f, b02GasIn.connWorld.z, 0.01f)

        // Test T-04.Out
        val t04Out = nozzlesMap["T-04.Out"]
        assertNotNull(t04Out)
        assertEquals(8.0f, t04Out.connWorld.x, 0.01f)
        assertEquals(5.35f, t04Out.connWorld.y, 0.01f)
        assertEquals(22.9f, t04Out.connWorld.z, 0.01f)

        // Test T-01.InTop
        val t01InTop = nozzlesMap["T-01.InTop"]
        assertNotNull(t01InTop)
        assertEquals(8.0f, t01InTop.connWorld.x, 0.01f)
        assertEquals(-10.5f, t01InTop.connWorld.y, 0.01f)
        assertEquals(5.7f, t01InTop.connWorld.z, 0.01f)

        // Test T-01.Suction
        val t01Suction = nozzlesMap["T-01.Suction"]
        assertNotNull(t01Suction)
        assertEquals(8.0f, t01Suction.connWorld.x, 0.01f)
        assertEquals(-13.5f, t01Suction.connWorld.y, 0.01f)
        assertEquals(0.9f, t01Suction.connWorld.z, 0.01f)
    }

    @Test
    fun testSceneHierarchyGeneration() {
        val config = FactorySceneConfig.loadFromResources()
        val scene = MogSceneBuilder.buildScene(config)

        assertNotNull(scene)
        val nmz = scene.getVision("NMZ".parseAsName()) as? SolidGroup
        assertNotNull(nmz)
        val mog = scene.getVision("NMZ.CPNSK.MOG".parseAsName()) as? SolidGroup
        assertNotNull(mog)

        val slab = scene.getVision("NMZ.CPNSK.MOG.Slab".parseAsName())
        assertNotNull(slab)

        for (i in 1..3) {
            val line = scene.getVision("NMZ.CPNSK.MOG.Line$i".parseAsName()) as? SolidGroup
            assertNotNull(line)

            val eqGroup = scene.getVision("NMZ.CPNSK.MOG.Line$i.Equipment".parseAsName()) as? SolidGroup
            assertNotNull(eqGroup)
            assertEquals(26, eqGroup.visions.size)

            val pipingGroup = scene.getVision("NMZ.CPNSK.MOG.Line$i.Piping".parseAsName()) as? SolidGroup
            assertNotNull(pipingGroup)
            assertEquals(26, pipingGroup.visions.size)
        }
    }

    @Test
    fun testDisplayModeSwitching() {
        val config = FactorySceneConfig.loadFromResources()
        val scene = MogSceneBuilder.buildScene(config)

        val b01 = scene.getVision("NMZ.CPNSK.MOG.Line1.Equipment.B_01".parseAsName())
        assertNotNull(b01)

        val g01 = scene.getVision("NMZ.CPNSK.MOG.Line1.Piping.G_01".parseAsName())
        assertNotNull(g01)

        // Switch to GOST 14202 mode
        space.kscience.visionforge.demo.factory.styles.MogStateManager.applyDisplayMode(
            scene, config, space.kscience.visionforge.demo.factory.styles.DisplayMode.GOST14202
        )
        assertEquals(listOf("material_Neutral"), b01.styles)
        assertEquals(listOf("gost_3"), g01.styles)

        // Switch to State mode
        space.kscience.visionforge.demo.factory.styles.MogStateManager.applyDisplayMode(
            scene, config, space.kscience.visionforge.demo.factory.styles.DisplayMode.STATE
        )
        assertEquals(listOf("state_run"), b01.styles)
        assertEquals(listOf("state_run"), g01.styles)

        // Switch back to Material mode
        space.kscience.visionforge.demo.factory.styles.MogStateManager.applyDisplayMode(
            scene, config, space.kscience.visionforge.demo.factory.styles.DisplayMode.MATERIAL
        )
        assertEquals(listOf("material_LinedSteel"), b01.styles)
        assertEquals(listOf("material_PaintedSteel"), g01.styles)
    }

    @Test
    fun testTelemetryStateOverride() {
        val config = FactorySceneConfig.loadFromResources()
        val scene = MogSceneBuilder.buildScene(config)

        val b03A = scene.getVision("NMZ.CPNSK.MOG.Line2.Equipment.B_03A".parseAsName())
        assertNotNull(b03A)

        space.kscience.visionforge.demo.factory.styles.MogStateManager.setEquipmentState(
            scene, 2, "B-03A", "flush", space.kscience.visionforge.demo.factory.styles.DisplayMode.STATE
        )

        assertEquals("flush", b03A.properties.getValue(space.kscience.dataforge.names.Name.of("dt:state"))?.string)
        assertEquals(listOf("state_flush"), b03A.styles)
    }
}
