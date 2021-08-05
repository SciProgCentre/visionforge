package space.kscience.visionforge.gdml

import space.kscience.dataforge.context.Context
import space.kscience.dataforge.names.toName
import space.kscience.gdml.*
import space.kscience.visionforge.Vision
import space.kscience.visionforge.get
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.visionManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal val testContext = Context("TEST") {
    plugin(Solids)
}

class TestCubes {

    val cubes = GdmlShowCase.cubes()

    @Test
    fun testCubesDirect() {
        val vision = cubes.toVision()
//        println(Solids.encodeToString(vision))
        val smallBoxPrototype = vision.getPrototype("solids.smallBox".toName()) as? Box
        assertNotNull(smallBoxPrototype)
        assertEquals(30.0, smallBoxPrototype.xSize.toDouble())
        val smallBoxVision = vision["composite-111.smallBox"]?.unref as? Box
        assertNotNull(smallBoxVision)
        assertEquals(30.0, smallBoxVision.xSize.toDouble())
    }

    @Test
    fun testGdmlExport() {
        val xml = cubes.encodeToString()
        //println(xml)
        val gdml = Gdml.decodeFromString(xml)
        val smallBox = gdml.getSolid<GdmlBox>("smallBox")
        assertNotNull(smallBox)
        assertEquals(30.0, smallBox.x.toDouble())
    }

    @Test
    fun testCubesReSerialize() {
        val vision = cubes.toVision()
        val serialized = Solids.encodeToString(vision)
        val deserialized = testContext.visionManager.decodeFromString(serialized) as SolidGroup
        val smallBox = deserialized.getPrototype("solids.smallBox".toName()) as? Box
        assertNotNull(smallBox)
        assertEquals(30.0, smallBox.xSize.toDouble())
        //println(testContext.visionManager.encodeToString(deserialized))
        fun Vision.checkPrototypes() {
            if (this is SolidReference) {
                assertNotNull(this.prototype)
            }
            if (this is SolidGroup) {
                children.forEach {
                    it.value.checkPrototypes()
                }
            }
        }
        deserialized.checkPrototypes()
    }
}