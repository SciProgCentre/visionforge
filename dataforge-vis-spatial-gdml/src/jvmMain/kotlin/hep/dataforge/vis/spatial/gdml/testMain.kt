package hep.dataforge.vis.spatial.gdml

import hep.dataforge.vis.spatial.Material3D
import hep.dataforge.vis.spatial.Visual3DPlugin
import hep.dataforge.vis.spatial.VisualGroup3D
import hep.dataforge.vis.spatial.opacity
import hep.dataforge.vis.spatial.transform.RemoveSingleChild
import hep.dataforge.vis.spatial.transform.UnRef
import nl.adaptivity.xmlutil.StAXReader
import scientifik.gdml.GDML
import java.io.File

fun main() {
    val file = File("D:\\Work\\Projects\\gdml.kt\\gdml-source\\BM@N.gdml")

    val xmlReader = StAXReader(file.inputStream(), "UTF-8")
    val xml = GDML.format.parse(GDML.serializer(), xmlReader)
    val visual = xml.toVisual {
        lUnit = LUnit.CM

        volumeAction = { volume ->
            when {
                volume.name.startsWith("ecal01lay") -> GDMLTransformer.Action.REJECT
                else -> GDMLTransformer.Action.CACHE
            }
        }

        solidConfiguration = { parent, solid ->
            if (parent.physVolumes.isNotEmpty()
                || solid.name.startsWith("Coil")
                || solid.name.startsWith("Yoke")
                || solid.name.startsWith("Magnet")
                || solid.name.startsWith("Pole")
            ) {
                useStyle("opaque") {
                    Material3D.OPACITY_KEY to 0.3
                }
            }
        }
    }

  //  (visual as? VisualGroup3D)?.let { UnRef(it) }?.let { RemoveSingleChild(it) }

    val string = Visual3DPlugin.json.stringify(VisualGroup3D.serializer(), visual)

    val tmpFile = File.createTempFile("dataforge-visual", ".json")

    tmpFile.writeText(string)

    println(tmpFile.canonicalPath)

//    val template = visual.getTemplate("volumes.ecal01mod".toName())
//    println(template)
//    visual.flatMap { (it as? VisualGroup3D) ?: listOf(it) }.forEach {
//        if(it.parent==null) error("")
//    }
    //readLine()
    //val meta = visual.toMeta()
//    val tmpFile = File.createTempFile("dataforge-visual", "json")
    //tmpFile.writeText(meta.toString())
    //println(tmpFile.absoluteFile)
}