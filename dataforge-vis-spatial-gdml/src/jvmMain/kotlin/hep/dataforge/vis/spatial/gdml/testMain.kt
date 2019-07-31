package hep.dataforge.vis.spatial.gdml

import nl.adaptivity.xmlutil.StAXReader
import scientifik.gdml.GDML
import java.io.File
import java.net.URL

fun main() {
    val url = URL("https://drive.google.com/open?id=1w5e7fILMN83JGgB8WANJUYm8OW2s0WVO")
    val file = File("D:\\Work\\Projects\\gdml.kt\\gdml-source\\BM@N.gdml")
    val stream = if (file.exists()) {
        file.inputStream()
    } else {
        url.openStream()
    }

    val xmlReader = StAXReader(stream, "UTF-8")
    val xml = GDML.format.parse(GDML.serializer(), xmlReader)
    xml.toVisual {
        lUnit = LUnit.CM
        //acceptSolid = { solid -> !solid.name.startsWith("ecal") && !solid.name.startsWith("V") }
        onFinish = { printStatistics() }
    }
    readLine()
}