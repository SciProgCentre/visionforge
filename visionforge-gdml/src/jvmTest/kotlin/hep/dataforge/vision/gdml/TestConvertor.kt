package hep.dataforge.vision.gdml

import hep.dataforge.names.toName
import hep.dataforge.vision.solid.Solids
import org.junit.jupiter.api.Test
import space.kscience.gdml.Gdml
import space.kscience.gdml.decodeFromStream
import kotlin.test.assertNotNull

class TestConvertor {

    @Test
    fun testBMNGeometry() {
        val stream = javaClass.getResourceAsStream("/gdml/BM@N.gdml")!!
        val gdml = Gdml.decodeFromStream(stream)
        val vision = gdml.toVision()
        //println(SolidManager.encodeToString(vision))
    }

    @Test
    fun testCubes() {
        val stream = javaClass.getResourceAsStream("/gdml/cubes.gdml")!!
        val gdml = Gdml.decodeFromStream(stream)
        val vision = gdml.toVision()
        assertNotNull(vision.getPrototype("solids.box".toName()))
        println(Solids.encodeToString(vision))
    }

    @Test
    fun testSimple() {
        val stream = javaClass.getResourceAsStream("/gdml/simple1.gdml")!!
        val gdml = Gdml.decodeFromStream(stream)
        val vision = gdml.toVision()
        //println(SolidManager.encodeToString(vision))
    }
}