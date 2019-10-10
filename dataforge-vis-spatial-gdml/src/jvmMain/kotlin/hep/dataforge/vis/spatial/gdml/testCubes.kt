package hep.dataforge.vis.spatial.gdml

import hep.dataforge.vis.spatial.Material3D
import hep.dataforge.vis.spatial.Visual3DPlugin
import hep.dataforge.vis.spatial.VisualGroup3D
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.transform.RemoveSingleChild
import hep.dataforge.vis.spatial.transform.UnRef
import nl.adaptivity.xmlutil.StAXReader
import scientifik.gdml.GDML
import java.io.File

fun main() {
    val file = File("D:\\Work\\Projects\\gdml.kt\\gdml-source\\cubes.gdml")

    val xmlReader = StAXReader(file.inputStream(), "UTF-8")
    val xml = GDML.format.parse(GDML.serializer(), xmlReader)
    val visual = xml.toVisual {
        lUnit = LUnit.CM

        solidConfiguration = { parent, solid ->
            if (parent.physVolumes.isNotEmpty()) {
                useStyle("opaque") {
                    Material3D.OPACITY_KEY to 0.3
                    VisualObject3D.LAYER_KEY to 2
                }
            }
        }
    }

    (visual as? VisualGroup3D)?.let { UnRef(it) }?.let { RemoveSingleChild(it) }

    val string = Visual3DPlugin.json.stringify(VisualGroup3D.serializer(), visual)

    val tmpFile = File.createTempFile("dataforge-visual", ".json")

    tmpFile.writeText(string)

    println(tmpFile.canonicalPath)
}