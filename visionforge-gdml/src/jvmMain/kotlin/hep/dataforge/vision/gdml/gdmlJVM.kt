package hep.dataforge.vision.gdml

import hep.dataforge.vision.solid.SolidGroup
import space.kscience.gdml.Gdml
import space.kscience.gdml.decodeFromFile
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

public actual typealias Counter = AtomicInteger

public fun SolidGroup.gdml(
    file: Path,
    key: String = "",
    usePreprocessor: Boolean = false,
    transformer: GdmlTransformerSettings.() -> Unit = {},
) {
    val gdml = Gdml.decodeFromFile(file, usePreprocessor)
    gdml(gdml, key, transformer)
}
