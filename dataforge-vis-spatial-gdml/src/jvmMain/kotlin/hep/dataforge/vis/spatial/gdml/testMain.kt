package hep.dataforge.vis.spatial.gdml

import hep.dataforge.vis.spatial.Visual3DPlugin
import hep.dataforge.vis.spatial.VisualGroup3D
import hep.dataforge.vis.spatial.opacity
import nl.adaptivity.xmlutil.StAXReader
import scientifik.gdml.GDML
import java.io.File

fun main() {
    val file = File("D:\\Work\\Projects\\gdml.kt\\gdml-source\\BM@N.gdml")
    //val file = File("D:\\Work\\Projects\\gdml.kt\\gdml-source\\cubes.gdml")

    val xmlReader = StAXReader(file.inputStream(), "UTF-8")
    val xml = GDML.format.parse(GDML.serializer(), xmlReader)
    val visual = xml.toVisual {
        lUnit = LUnit.CM
        volumeAction = { volume ->
            when {
                volume.name.startsWith("ecal01lay") -> GDMLTransformer.Action.REJECT
                volume.name.startsWith("ecal") -> GDMLTransformer.Action.CACHE
                volume.name.startsWith("UPBL") -> GDMLTransformer.Action.REJECT
                volume.name.startsWith("USCL") -> GDMLTransformer.Action.REJECT
                volume.name.startsWith("U") -> GDMLTransformer.Action.CACHE
                volume.name.startsWith("VPBL") -> GDMLTransformer.Action.REJECT
                volume.name.startsWith("VSCL") -> GDMLTransformer.Action.REJECT
                volume.name.startsWith("V") -> GDMLTransformer.Action.CACHE
                else -> GDMLTransformer.Action.ACCEPT
            }
        }

        solidConfiguration = { parent, solid ->
            if (parent.physVolumes.isNotEmpty()) {
                opacity = 0.3
            }
            if (solid.name.startsWith("Coil")
                || solid.name.startsWith("Yoke")
                || solid.name.startsWith("Magnet")
                || solid.name.startsWith("Pole")
            ) {
                opacity = 0.3
            }
        }

//        optimizeSingleChild = true
        //optimizations = listOf(optimizeSingleChild)
        onFinish = { printStatistics() }
    }

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