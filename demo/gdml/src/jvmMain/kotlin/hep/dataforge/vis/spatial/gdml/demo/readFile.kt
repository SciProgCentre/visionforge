package hep.dataforge.vis.spatial.gdml.demo

import hep.dataforge.meta.setItem
import hep.dataforge.values.asValue
import hep.dataforge.vis.spatial.Material3D
import hep.dataforge.vis.spatial.Visual3D
import hep.dataforge.vis.spatial.VisualGroup3D
import hep.dataforge.vis.spatial.gdml.LUnit
import hep.dataforge.vis.spatial.gdml.readFile
import hep.dataforge.vis.spatial.gdml.toVisual
import scientifik.gdml.GDML
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

fun Visual3D.Companion.readFile(file: File): VisualGroup3D = when {
    file.extension == "gdml" || file.extension == "xml" -> {
        GDML.readFile(file.toPath()).toVisual {
            lUnit = LUnit.CM

            solidConfiguration = { parent, solid ->
                if (solid.name == "cave") {
                    setItem(Material3D.MATERIAL_WIREFRAME_KEY, true.asValue())
                }
                if (parent.physVolumes.isNotEmpty()) {
                    useStyle("opaque") {
                        Material3D.MATERIAL_OPACITY_KEY put 0.3
                    }
                }
            }
        }
    }
    file.extension == "json" -> VisualGroup3D.parseJson(file.readText())
    file.name.endsWith("json.zip") -> {
        file.inputStream().use {
            val unzip = ZipInputStream(it, Charsets.UTF_8)
            val text = unzip.readBytes().decodeToString()
            VisualGroup3D.parseJson(text)
        }
    }
    file.name.endsWith("json.gz") -> {
        file.inputStream().use {
            val unzip = GZIPInputStream(it)
            val text = unzip.readBytes().decodeToString()
            VisualGroup3D.parseJson(text)
        }
    }
    else -> error("Unknown extension ${file.extension}")
}

fun Visual3D.Companion.readFile(fileName: String): VisualGroup3D = readFile(File(fileName))