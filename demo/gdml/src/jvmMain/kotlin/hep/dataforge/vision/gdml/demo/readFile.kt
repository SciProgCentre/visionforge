package hep.dataforge.vision.gdml.demo

import hep.dataforge.misc.DFExperimental
import hep.dataforge.values.asValue
import hep.dataforge.vision.gdml.readFile
import hep.dataforge.vision.gdml.toVision
import hep.dataforge.vision.solid.SolidGroup
import hep.dataforge.vision.solid.SolidManager
import hep.dataforge.vision.solid.SolidMaterial
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionManager
import kscience.gdml.GDML
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

@OptIn(DFExperimental::class)
fun VisionManager.readFile(file: File): Vision = when {
    file.extension == "gdml" || file.extension == "xml" -> {
        GDML.readFile(file.toPath()).toVision {
//            lUnit = LUnit.CM
//
//            solidConfiguration = { parent, solid ->
//                if (solid.name == "cave") {
//                    setItem(SolidMaterial.MATERIAL_WIREFRAME_KEY, true.asValue())
//                }
//                if (parent.physVolumes.isNotEmpty()) {
//                    useStyle("opaque") {
//                        SolidMaterial.MATERIAL_OPACITY_KEY put 0.3
//                    }
//                }
//            }
        }
    }
    file.extension == "json" -> decodeFromString(file.readText())
    file.name.endsWith("json.zip") -> {
        file.inputStream().use {
            val unzip = ZipInputStream(it, Charsets.UTF_8)
            val text = unzip.readBytes().decodeToString()
            decodeFromString(text)
        }
    }
    file.name.endsWith("json.gz") -> {
        file.inputStream().use {
            val unzip = GZIPInputStream(it)
            val text = unzip.readBytes().decodeToString()
            decodeFromString(text)
        }
    }
    else -> error("Unknown extension ${file.extension}")
}

@OptIn(DFExperimental::class)
fun VisionManager.readFile(fileName: String): Vision = readFile(File(fileName))