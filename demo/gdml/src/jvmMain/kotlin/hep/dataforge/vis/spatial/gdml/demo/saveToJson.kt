package hep.dataforge.vis.spatial.gdml.demo

import hep.dataforge.vis.spatial.Visual3DPlugin
import hep.dataforge.vis.spatial.VisualGroup3D
import hep.dataforge.vis.spatial.gdml.LUnit
import hep.dataforge.vis.spatial.gdml.readFile
import hep.dataforge.vis.spatial.gdml.toVisual
import scientifik.gdml.GDML
import java.io.File
import java.nio.file.Paths

fun main() {
    val gdml = GDML.readFile(Paths.get("D:\\Work\\Projects\\gdml.kt\\gdml-source\\cubes.gdml"))
    val visual = gdml.toVisual {
        lUnit = LUnit.CM
    }
    val json = Visual3DPlugin.json.stringify(VisualGroup3D.serializer(), visual)
    File("D:\\Work\\Projects\\gdml.kt\\gdml-source\\cubes.json").writeText(json)
}