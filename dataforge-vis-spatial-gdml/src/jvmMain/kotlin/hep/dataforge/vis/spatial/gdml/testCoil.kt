package hep.dataforge.vis.spatial.gdml

import hep.dataforge.vis.spatial.Visual3DPlugin
import hep.dataforge.vis.spatial.VisualGroup3D
import nl.adaptivity.xmlutil.StAXReader
import scientifik.gdml.GDML
import java.io.File


fun main() {
    val file = File("D:\\Work\\Projects\\gdml.kt\\gdml-source\\BM@N_coil.gdml")

    val xmlReader = StAXReader(file.inputStream(), "UTF-8")
    val xml = GDML.format.parse(GDML.serializer(), xmlReader)
    val visual = xml.toVisual {
        lUnit = LUnit.CM
    }

    //val meta = visual.toMeta()

    val str = Visual3DPlugin.json.stringify(VisualGroup3D.serializer(), visual)

    println(str)

    //println(Json.indented.stringify(meta.toJson()))
}