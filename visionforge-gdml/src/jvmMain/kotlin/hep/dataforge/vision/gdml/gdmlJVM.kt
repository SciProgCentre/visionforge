package hep.dataforge.vision.gdml

import hep.dataforge.vision.solid.SolidGroup
import nl.adaptivity.xmlutil.StAXReader
import space.kscience.gdml.Gdml
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

public actual typealias Counter = AtomicInteger

public fun Gdml.Companion.readFile(file: Path): Gdml {
    val xmlReader = StAXReader(Files.newInputStream(file), "UTF-8")
    return format.decodeFromReader(Gdml.serializer(), xmlReader)
}

public fun SolidGroup.gdml(file: Path, key: String = "", transformer: GdmlTransformerSettings.() -> Unit = {}) {
    val gdml = Gdml.readFile(file)
    gdml(gdml, key, transformer)
}
