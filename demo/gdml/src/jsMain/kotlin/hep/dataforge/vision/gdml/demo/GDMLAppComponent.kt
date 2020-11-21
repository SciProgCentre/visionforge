package hep.dataforge.vision.gdml.demo

import hep.dataforge.context.Context
import hep.dataforge.names.Name
import hep.dataforge.vision.Vision
import hep.dataforge.vision.bootstrap.gridRow
import hep.dataforge.vision.bootstrap.nameCrumbs
import hep.dataforge.vision.bootstrap.threeControls
import hep.dataforge.vision.gdml.toVision
import hep.dataforge.vision.react.ThreeCanvasComponent
import hep.dataforge.vision.react.flexColumn
import hep.dataforge.vision.solid.Solid
import hep.dataforge.vision.solid.SolidManager
import hep.dataforge.vision.solid.specifications.Canvas3DOptions
import hep.dataforge.vision.solid.three.ThreeCanvas
import kotlinx.browser.window
import kotlinx.css.*
import kscience.gdml.GDML
import kscience.gdml.decodeFromString
import org.w3c.files.FileReader
import org.w3c.files.get
import react.RProps
import react.child
import react.dom.h1
import react.functionalComponent
import react.useState
import styled.css
import styled.styledDiv

external interface GDMLAppProps : RProps {
    var context: Context
    var rootObject: Vision?
    var selected: Name?
}

//private val canvasConfig = Canvas3DOptions {
//    camera = Camera {
//        distance = 2100.0
//        latitude = PI / 6
//        azimuth = PI + PI / 6
//    }
//}

@JsExport
val GDMLApp = functionalComponent<GDMLAppProps>("GDMLApp") { props ->
    var selected by useState { props.selected }
    var canvas: ThreeCanvas? by useState { null }
    var vision: Vision? by useState { props.rootObject }

    val onSelect: (Name?) -> Unit = {
        selected = it
    }

    fun loadData(name: String, data: String) {
        val visionManager = props.context.plugins.fetch(SolidManager).visionManager
        val parsedVision = when {
            name.endsWith(".gdml") || name.endsWith(".xml") -> {
                val gdml = GDML.decodeFromString(data)
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
            css{
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
                threeControls(it, selected, onSelect)
            }
        }
    }
}
