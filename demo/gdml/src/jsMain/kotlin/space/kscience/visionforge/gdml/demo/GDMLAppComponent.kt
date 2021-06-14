package space.kscience.visionforge.gdml.demo

import kotlinx.browser.window
import org.w3c.files.FileReader
import org.w3c.files.get
import react.*
import react.dom.h2
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.fetch
import space.kscience.dataforge.names.Name
import space.kscience.gdml.Gdml
import space.kscience.gdml.decodeFromString
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.ring.ThreeCanvasWithControls
import space.kscience.visionforge.ring.tab
import space.kscience.visionforge.root
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.Solids

external interface GDMLAppProps : RProps {
    var context: Context
    var vision: Solid?
    var selected: Name?
}

@JsExport
val GDMLApp = functionalComponent<GDMLAppProps>("GDMLApp") { props ->
    val visionManager = useMemo(props.context) { props.context.fetch(Solids).visionManager }
    var vision: Solid? by useState { props.vision?.apply { root(visionManager) } }

    fun loadData(name: String, data: String) {
        val parsedVision = when {
            name.endsWith(".gdml") || name.endsWith(".xml") -> {
                val gdml = Gdml.decodeFromString(data)
                gdml.toVision()
            }
            name.endsWith(".json") -> visionManager.decodeFromString(data)
            else -> {
                window.alert("File extension is not recognized: $name")
                error("File extension is not recognized: $name")
            }
        }

        vision = parsedVision as? Solid ?: error("Parsed vision is not a solid")
    }

    child(ThreeCanvasWithControls) {
        attrs {
            this.context = props.context
            this.solid = vision
            this.selected = props.selected
            tab("Load") {
                h2 {
                    +"Drag and drop .gdml or .json VisionForge files here"
                }
                fileDrop("(drag file here)") { files ->
                    val file = files?.get(0)
                    if (file != null) {
                        FileReader().apply {
                            onload = {
                                val string = result as String
                                loadData(file.name, string)
                            }
                            readAsText(file)
                        }
                    }
                }
            }
        }

    }
}

