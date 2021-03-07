package space.kscience.visionforge.gdml.demo

import space.kscience.dataforge.misc.DFExperimental
import space.kscience.gdml.Gdml
import space.kscience.gdml.decodeFromFile
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.gdml.toVision
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

@OptIn(DFExperimental::class)
fun VisionManager.readFile(file: File): Vision = when {
    file.extension == "gdml" || file.extension == "xml" -> {
        Gdml.decodeFromFile(file.toPath(),true).toVision()
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