package hep.dataforge.vision.gdml

import hep.dataforge.vision.solid.SolidGroup
import kscience.gdml.GDML
import nl.adaptivity.xmlutil.StAXReader
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

public actual typealias Counter = AtomicInteger

public fun GDML.Companion.readFile(file: Path): GDML {
    val xmlReader = StAXReader(Files.newInputStream(file), "UTF-8")
    return format.parse(GDML.serializer(), xmlReader)
}

public fun SolidGroup.gdml(file: Path, key: String = "", transformer: GDMLTransformerSettings.() -> Unit = {}) {
    val gdml = GDML.readFile(file)
    gdml(gdml, key, transformer)
}
