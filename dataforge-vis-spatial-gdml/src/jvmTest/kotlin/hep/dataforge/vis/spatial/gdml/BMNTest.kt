package hep.dataforge.vis.spatial.gdml

import nl.adaptivity.xmlutil.StAXReader
import org.junit.Test
import scientifik.gdml.GDML
import java.io.File
import java.net.URL

class BMNTest {

    @Test
    fun testRead() {

        val url = URL("https://drive.google.com/open?id=1w5e7fILMN83JGgB8WANJUYm8OW2s0WVO")
        val file = File("D:\\Work\\Projects\\gdml.kt\\src\\commonTest\\resources\\gdml\\geofile_full.xml")
        val stream = if (file.exists()) {
            file.inputStream()
        } else {
            url.openStream()
        }

        val xmlReader = StAXReader(stream, "UTF-8")
        val xml = GDML.format.parse(GDML.serializer(), xmlReader)
        repeat(20) {
            xml.toVisual()
        }
    }
}