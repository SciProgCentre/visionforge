package space.kscience.visionforge.gdml.demo

import androidx.compose.runtime.*
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.files.get
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.names.Name
import space.kscience.gdml.Gdml
import space.kscience.gdml.decodeFromString
import space.kscience.visionforge.Colors
import space.kscience.visionforge.gdml.markLayers
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.setAsRoot
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.ambientLight
import space.kscience.visionforge.solid.invoke
import space.kscience.visionforge.solid.three.compose.ThreeView
import space.kscience.visionforge.visionManager


@Composable
fun GDMLApp(context: Context, initialVision: Solid?, selected: Name? = null) {
    var vision: Solid? by remember { mutableStateOf(initialVision) }

    fun readFileAsync(file: File) {
        val visionManager = context.visionManager
        FileReader().apply {
            onload = {
                val data = result as String
                val name = file.name
                val parsedVision = when {
                    name.endsWith(".gdml") || name.endsWith(".xml") -> {
                        val gdml = Gdml.decodeFromString(data)
                        gdml.toVision().apply {
                            setAsRoot(visionManager)
                            console.info("Marking layers for file $name")
                            markLayers()
                            ambientLight {
                                color(Colors.white)
                            }
                        }
                    }

                    name.endsWith(".json") -> visionManager.decodeFromString(data)
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
        ThreeView(context, vision, selected) {
            Tab("Load") {
                P {
                    Text("Drag and drop .gdml or .json VisionForge files here")
                }
                FileDrop("(drag file here)") { files ->
                    val file = files[0]
                    if (file != null) {
                        readFileAsync(file)
                    }
                }
            }
        }
    }
}

