package space.kscience.visionforge.gdml

import org.junit.jupiter.api.Test
import space.kscience.dataforge.names.Name
import space.kscience.gdml.Gdml
import space.kscience.gdml.decodeFromStream
import space.kscience.visionforge.solid.Solids
import kotlin.test.assertNotNull

@Suppress("UNUSED_VARIABLE")
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
        assertNotNull(vision.getPrototype(Name.parse("solids.box")))
        println(Solids.encodeToString(vision))
    }

    @Test
    fun testSimple() {
        val stream = javaClass.getResourceAsStream("/gdml/simple1.gdml")!!
        val gdml = Gdml.decodeFromStream(stream)
        val vision = gdml.toVision()
        //println(SolidManager.encodeToString(vision))
    }

    @Test
    fun testIaxo() {
        val stream = javaClass.getResourceAsStream("/gdml/babyIAXO.gdml")!!
        val gdml = Gdml.decodeFromStream(stream, true)
        val vision = gdml.toVision()
        println(Solids.encodeToString(vision))
     }
}