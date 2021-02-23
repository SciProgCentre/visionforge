package hep.dataforge.vision.gdml

import hep.dataforge.vision.solid.SolidManager
import space.kscience.gdml.Gdml
import nl.adaptivity.xmlutil.StAXReader
import org.junit.jupiter.api.Test

class TestConvertor {

    @Test
    fun testBMNGeometry() {
        val stream = javaClass.getResourceAsStream("/gdml/BM@N.gdml")
        val xmlReader = StAXReader(stream, "UTF-8")
        val xml = Gdml.format.parse(Gdml.serializer(), xmlReader)
        val vision = xml.toVision()
        println(SolidManager.encodeToString(vision))
    }

    @Test
    fun testCubes() {
        val stream = javaClass.getResourceAsStream("/gdml/cubes.gdml")

        val xmlReader = StAXReader(stream, "UTF-8")
        val xml = Gdml.format.parse(Gdml.serializer(), xmlReader)
        val visual = xml.toVision()
     //   println(visual)
    }

    @Test
    fun testSimple() {
        val stream = javaClass.getResourceAsStream("/gdml/simple1.gdml")

        val xmlReader = StAXReader(stream, "UTF-8")
        val xml = Gdml.format.parse(Gdml.serializer(), xmlReader)
        val vision = xml.toVision()
        println(SolidManager.encodeToString(vision))
    }
}