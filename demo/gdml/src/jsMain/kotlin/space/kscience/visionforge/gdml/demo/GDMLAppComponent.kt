package space.kscience.visionforge.gdml.demo

import androidx.compose.runtime.*
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.files.get
import space.kscience.dataforge.names.Name
import space.kscience.gdml.Gdml
import space.kscience.gdml.decodeFromString
import space.kscience.visionforge.Colors
import space.kscience.visionforge.gdml.markLayers
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.setAsRoot
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.ambientLight
import space.kscience.visionforge.solid.invoke
import space.kscience.visionforge.solid.three.compose.ThreeView


@Composable
fun GDMLApp(solids: Solids, initialVision: Solid?, selected: Name? = null) {
    var vision: Solid? by remember { mutableStateOf(initialVision) }

    fun readFileAsync(file: File) {
        FileReader().apply {
            onload = {
                val data = result as String
                val name = file.name
                val parsedVision = when {
                    name.endsWith(".gdml") || name.endsWith(".xml") -> {
                        val gdml = Gdml.decodeFromString(data)
                        gdml.toVision().apply {
                            setAsRoot(solids.visionManager)
                            console.info("Marking layers for file $name")
                            markLayers()
                            ambientLight {
                                color(Colors.white)
                            }
                        }
                    }

                    name.endsWith(".json") -> solids.visionManager.decodeFromString(data)
                    else -> {
                        window.alert("File extension is not recognized: $name")
                        error("File extension is not recognized: $name")
                    }
                }
                vision = parsedVision as? Solid ?: error("Parsed vision is not a solid")
                Unit
            }
            readAsText(file)
        }
    }

    Div({
        style {
            height(100.vh - 12.pt)
            width(100.vw)
        }
    }) {
        ThreeView(solids, vision, selected) {
            Tab("Load") {
                H2 {
                    Text("Drag and drop .gdml or .json VisionForge files here")
                }
                fileDrop("(drag file here)") { files ->
                    val file = files?.get(0)
                    if (file != null) {
                        readFileAsync(file)
                    }
                }
            }
        }
    }
}

