package hep.dataforge.vision.gdml

import hep.dataforge.vision.solid.SolidGroup
import nl.adaptivity.xmlutil.StAXReader
import scientifik.gdml.GDML
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

actual typealias Counter = AtomicInteger

fun GDML.Companion.readFile(file: Path): GDML {
    val xmlReader = StAXReader(Files.newInputStream(file), "UTF-8")
    return format.parse(GDML.serializer(), xmlReader)
}

fun SolidGroup.gdml(file: Path, key: String = "", transformer: GDMLTransformer.() -> Unit = {}) {
    val gdml = GDML.readFile(file)
    gdml(gdml, key, transformer)
}
