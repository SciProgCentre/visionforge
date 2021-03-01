package hep.dataforge.vision.gdml

import hep.dataforge.context.Context
import hep.dataforge.names.toName
import hep.dataforge.vision.Vision
import hep.dataforge.vision.gdml.GdmlShowcase.cubes
import hep.dataforge.vision.get
import hep.dataforge.vision.solid.*
import hep.dataforge.vision.visionManager
import space.kscience.gdml.Gdml
import space.kscience.gdml.GdmlBox
import space.kscience.gdml.decodeFromString
import space.kscience.gdml.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal val testContext = Context("TEST") {
    plugin(Solids)
}

class TestCubes {

    @Test
    fun testCubesDirect() {
        val vision = cubes.toVision()
        val smallBoxPrototype = vision.getPrototype("solids.smallBox".toName()) as? Box
        assertNotNull(smallBoxPrototype)
        assertEquals(30.0, smallBoxPrototype.xSize.toDouble())
        val smallBoxVision = vision["composite[1,1,1].smallBox"]?.prototype as? Box
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