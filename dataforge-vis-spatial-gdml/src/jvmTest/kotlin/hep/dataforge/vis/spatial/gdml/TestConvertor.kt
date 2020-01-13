package hep.dataforge.vis.spatial.gdml

import nl.adaptivity.xmlutil.StAXReader
import org.junit.Test
import scientifik.gdml.GDML
import java.io.File
import java.net.URL
import kotlin.test.Ignore

class TestConvertor {

    @Test
    @Ignore
    fun testBMNGeometry() {
        val url = URL("https://drive.google.com/open?id=1w5e7fILMN83JGgB8WANJUYm8OW2s0WVO")
        val file = File("D:\\Work\\Projects\\gdml.kt\\gdml-source\\BM@N.gdml")
        val stream = if (file.exists()) {
            file.inputStream()
        } else {
            url.openStream()
        }

        val xmlReader = StAXReader(stream, "UTF-8")
        val xml = GDML.format.parse(GDML.serializer(), xmlReader)
        xml.toVisual()
    }

    @Test
    @Ignore
    fun testCubes() {
        val file = File("D:\\Work\\Projects\\gdml.kt\\gdml-source\\cubes.gdml ")
        val stream = if (file.exists()) {
            file.inputStream()
        } else {
            return
        }

        val xmlReader = StAXReader(stream, "UTF-8")
        val xml = GDML.format.parse(GDML.serializer(), xmlReader)
        val visual = xml.toVisual()
        println(visual)
    }
}