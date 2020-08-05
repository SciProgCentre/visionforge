package hep.dataforge.vision.spatial.gdml

import hep.dataforge.vision.spatial.stringify
import nl.adaptivity.xmlutil.StAXReader
import org.junit.jupiter.api.Test
import scientifik.gdml.GDML

class TestConvertor {

    @Test
    fun testBMNGeometry() {
        val stream = javaClass.getResourceAsStream("/gdml/BM@N.gdml")

        val xmlReader = StAXReader(stream, "UTF-8")
        val xml = GDML.format.parse(GDML.serializer(), xmlReader)
        val visual = xml.toVisual()
        println(visual.stringify())
    }

    @Test
    fun testCubes() {
        val stream = javaClass.getResourceAsStream("/gdml/cubes.gdml")

        val xmlReader = StAXReader(stream, "UTF-8")
        val xml = GDML.format.parse(GDML.serializer(), xmlReader)
        val visual = xml.toVisual()
     //   println(visual)
    }

    @Test
    fun testSimple() {
        val stream = javaClass.getResourceAsStream("/gdml/simple1.gdml")

        val xmlReader = StAXReader(stream, "UTF-8")
        val xml = GDML.format.parse(GDML.serializer(), xmlReader)
        val visual = xml.toVisual()
        println(visual.stringify())
    }
}