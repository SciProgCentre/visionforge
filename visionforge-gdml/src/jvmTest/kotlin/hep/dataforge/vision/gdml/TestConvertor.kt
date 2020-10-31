package hep.dataforge.vision.gdml

import hep.dataforge.vision.solid.SolidManager
import kscience.gdml.GDML
import nl.adaptivity.xmlutil.StAXReader
import org.junit.jupiter.api.Test

class TestConvertor {

    @Test
    fun testBMNGeometry() {
        val stream = javaClass.getResourceAsStream("/gdml/BM@N.gdml")
        val xmlReader = StAXReader(stream, "UTF-8")
        val xml = GDML.format.parse(GDML.serializer(), xmlReader)
        val vision = xml.toVision()
        println(SolidManager.encodeToString(vision))
    }

    @Test
    fun testCubes() {
        val stream = javaClass.getResourceAsStream("/gdml/cubes.gdml")

        val xmlReader = StAXReader(stream, "UTF-8")
        val xml = GDML.format.parse(GDML.serializer(), xmlReader)
        val visual = xml.toVision()
     //   println(visual)
    }

    @Test
    fun testSimple() {
        val stream = javaClass.getResourceAsStream("/gdml/simple1.gdml")

        val xmlReader = StAXReader(stream, "UTF-8")
        val xml = GDML.format.parse(GDML.serializer(), xmlReader)
        val vision = xml.toVision()
        println(SolidManager.encodeToString(vision))
    }
}