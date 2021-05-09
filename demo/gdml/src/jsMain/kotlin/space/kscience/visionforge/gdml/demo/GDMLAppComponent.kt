package space.kscience.visionforge.gdml.demo

import kotlinx.browser.window
import kotlinx.css.*
import org.w3c.files.FileReader
import org.w3c.files.get
import react.*
import react.dom.h1
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.fetch
import space.kscience.dataforge.names.Name
import space.kscience.gdml.Gdml
import space.kscience.gdml.decodeFromString
import space.kscience.visionforge.Vision
import space.kscience.visionforge.bootstrap.gridRow
import space.kscience.visionforge.bootstrap.nameCrumbs
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.react.ThreeCanvasComponent
import space.kscience.visionforge.react.flexColumn
import space.kscience.visionforge.ring.ringThreeControls
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import space.kscience.visionforge.solid.three.ThreeCanvas
import styled.css
import styled.styledDiv

external interface GDMLAppProps : RProps {
    var context: Context
    var rootVision: Vision?
    var selected: Name?
}

@JsExport
val GDMLApp = functionalComponent<GDMLAppProps>("GDMLApp") { props ->
    var selected by useState { props.selected }
    var canvas: ThreeCanvas? by useState { null }
    var vision: Vision? by useState { props.rootVision }

    val onSelect: (Name?) -> Unit = {
        selected = it
    }

    val visionManager = useMemo({ props.context.fetch(Solids).visionManager }, arrayOf(props.context))

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

        vision = parsedVision
    }

    gridRow {
        flexColumn {
            css {
                +"col-lg-9"
                height = 100.vh
            }
            styledDiv {
                css {
                    +"mx-auto"
                    +"page-header"
                }
                h1 { +"GDML/JSON loader demo" }
            }
            nameCrumbs(selected, "World", onSelect)
            //canvas

            child(ThreeCanvasComponent) {
                attrs {
                    this.context = props.context
                    this.obj = vision as? Solid
                    this.selected = selected
                    this.options = Canvas3DOptions.invoke {
                        this.onSelect = onSelect
                    }
                    this.canvasCallback = {
                        canvas = it
                    }
                }
            }

        }
        flexColumn {
            css {
                +"col-lg-3"
                padding(top = 4.px)
                //border(1.px, BorderStyle.solid, Color.lightGray)
                height = 100.vh
                overflowY = Overflow.auto
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
            canvas?.let {
                ringThreeControls(it, selected, onSelect)
            }
        }
    }
}
