package space.kscience.visionforge.demo.factory

import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.parseAsName
import space.kscience.visionforge.demo.factory.model.FactorySceneConfig
import space.kscience.visionforge.demo.factory.scene.MogEquipmentBuilder
import space.kscience.visionforge.demo.factory.scene.MogSceneBuilder
import space.kscience.visionforge.demo.factory.styles.DisplayMode
import space.kscience.visionforge.demo.factory.styles.MogStateManager
import space.kscience.visionforge.demo.factory.styles.MogStyles
import space.kscience.visionforge.solid.*
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

        val b01Shell = scene.getVision("NMZ.CPNSK.MOG.Line1.Equipment.B_01.Geom.Shell".parseAsName()) as? Solid
        assertNotNull(b01Shell)

        val b01Plinth = scene.getVision("NMZ.CPNSK.MOG.Line1.Equipment.B_01.Geom.Plinth".parseAsName()) as? Solid
        assertNotNull(b01Plinth)

        val g01Seg = scene.getVision("NMZ.CPNSK.MOG.Line1.Piping.G_01.Seg_00".parseAsName()) as? Solid
        assertNotNull(g01Seg)

        val linedSteelColor = MogStyles.rgbToHex(config.materials["LinedSteel"]!!.rgb)
        val concreteColor = MogStyles.rgbToHex(config.materials["Concrete"]!!.rgb)
        val paintedSteelColor = MogStyles.rgbToHex(config.materials["PaintedSteel"]!!.rgb)
        val neutralColor = MogStyles.rgbToHex(config.materials["Neutral"]!!.rgb)
        val gost3Color = MogStyles.rgbToHex(config.gost14202["3"]!!.rgb)
        val stateRunColor = MogStyles.rgbToHex(config.states["run"]!!.rgb)

        // Initial Material mode
        assertEquals(listOf(MogStyles.HIGHLIGHT_STYLE_NAME, "material_LinedSteel"), b01.styles)
        assertEquals(listOf(MogStyles.HIGHLIGHT_STYLE_NAME, "material_PaintedSteel"), g01.styles)
        assertEquals(linedSteelColor, b01Shell.color.string)
        assertEquals(concreteColor, b01Plinth.color.string)
        assertEquals(paintedSteelColor, g01Seg.color.string)

        // Switch to GOST 14202 mode
        MogStateManager.applyDisplayMode(scene, config, DisplayMode.GOST14202)
        assertEquals(listOf(MogStyles.HIGHLIGHT_STYLE_NAME, "material_LinedSteel"), b01.styles)
        assertEquals(listOf(MogStyles.HIGHLIGHT_STYLE_NAME, "material_PaintedSteel"), g01.styles)
        assertEquals(neutralColor, b01Shell.color.string)
        assertEquals(neutralColor, b01Plinth.color.string)
        assertEquals(gost3Color, g01Seg.color.string)

        // Switch to State mode
        MogStateManager.applyDisplayMode(scene, config, DisplayMode.STATE)
        assertEquals(listOf(MogStyles.HIGHLIGHT_STYLE_NAME, "material_LinedSteel"), b01.styles)
        assertEquals(listOf(MogStyles.HIGHLIGHT_STYLE_NAME, "material_PaintedSteel"), g01.styles)
        assertEquals(stateRunColor, b01Shell.color.string)
        assertEquals(stateRunColor, b01Plinth.color.string)
        assertEquals(stateRunColor, g01Seg.color.string)

        // Switch back to Material mode
        MogStateManager.applyDisplayMode(scene, config, DisplayMode.MATERIAL)
        assertEquals(listOf(MogStyles.HIGHLIGHT_STYLE_NAME, "material_LinedSteel"), b01.styles)
        assertEquals(listOf(MogStyles.HIGHLIGHT_STYLE_NAME, "material_PaintedSteel"), g01.styles)
        assertEquals(linedSteelColor, b01Shell.color.string)
        assertEquals(concreteColor, b01Plinth.color.string)
        assertEquals(paintedSteelColor, g01Seg.color.string)
    }

    @Test
    fun testTelemetryStateOverride() {
        val config = FactorySceneConfig.loadFromResources()
        val scene = MogSceneBuilder.buildScene(config)

        val b03A = scene.getVision("NMZ.CPNSK.MOG.Line2.Equipment.B_03A".parseAsName())
        assertNotNull(b03A)

        val b03AInlet = scene.getVision("NMZ.CPNSK.MOG.Line2.Equipment.B_03A.Geom.InletSection".parseAsName()) as? Solid
        assertNotNull(b03AInlet)

        val stateRunColor = MogStyles.rgbToHex(config.states["run"]!!.rgb)
        val stateFlushColor = MogStyles.rgbToHex(config.states["flush"]!!.rgb)

        MogStateManager.applyDisplayMode(scene, config, DisplayMode.STATE)
        assertEquals(stateRunColor, b03AInlet.color.string)

        MogStateManager.setEquipmentState(
            scene, 2, "B-03A", "flush", DisplayMode.STATE
        )

        assertEquals("flush", b03A.properties.getValue(space.kscience.dataforge.names.Name.of("dt:state"))?.string)
        assertEquals(listOf(MogStyles.HIGHLIGHT_STYLE_NAME, "material_FRP"), b03A.styles)
        assertEquals(stateFlushColor, b03AInlet.color.string)
    }
}
