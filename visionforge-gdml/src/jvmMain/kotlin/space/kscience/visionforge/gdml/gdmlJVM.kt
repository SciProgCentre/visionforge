package space.kscience.visionforge.gdml

import space.kscience.gdml.Gdml
import space.kscience.gdml.decodeFromFile
import space.kscience.visionforge.solid.SolidGroup
import java.nio.file.Path

public fun SolidGroup.gdml(
    file: Path,
    key: String = "",
    usePreprocessor: Boolean = false,
    transformer: GdmlTransformer.() -> Unit = {},
) {
    val gdml = Gdml.decodeFromFile(file, usePreprocessor)
    gdml(gdml, key, transformer)
}
