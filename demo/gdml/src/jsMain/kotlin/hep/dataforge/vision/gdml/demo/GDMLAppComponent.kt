package hep.dataforge.vision.gdml.demo

import hep.dataforge.context.Context
import hep.dataforge.names.Name
import hep.dataforge.names.isEmpty
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionGroup
import hep.dataforge.vision.bootstrap.*
import hep.dataforge.vision.gdml.toVision
import hep.dataforge.vision.react.objectTree
import hep.dataforge.vision.solid.Solid
import hep.dataforge.vision.solid.SolidGroup
import hep.dataforge.vision.solid.three.ThreeCanvas
import hep.dataforge.vision.solid.three.ThreeCanvasComponent
import kotlinx.browser.window
import kotlinx.css.FlexBasis
import kotlinx.css.Overflow
import kotlinx.css.flex
import kotlinx.css.overflow
import kscience.gdml.GDML
import kscience.gdml.decodeFromString
import org.w3c.files.FileReader
import org.w3c.files.get
import react.*
import react.dom.h1
import styled.css

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

    val select: (Name?) -> Unit = {
        selected = it
    }

    fun loadData(name: String, data: String) {
        val parsedVision = when {
            name.endsWith(".gdml") || name.endsWith(".xml") -> {
                val gdml = GDML.decodeFromString(data)
                gdml.toVision()
            }
            name.endsWith(".json") -> SolidGroup.decodeFromString(data)
            else -> {
                window.alert("File extension is not recognized: $name")
                error("File extension is not recognized: $name")
            }
        }

        vision = parsedVision
    }

    gridColumn {
        css {
            flex(1.0, 1.0, FlexBasis.auto)
        }
        h1 { +"GDML/JSON loader demo" }
        gridRow {
            css {
                +"p-1"
                overflow = Overflow.auto
            }
            gridColumn(3, maxSize = GridMaxSize.XL) {
                css {
                    +"order-2"
                    +"order-xl-1"
                }
                card("Load data") {
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
                //tree
                card("Object tree") {
                    vision?.let {
                        objectTree(it, selected, select)
                    }
                }
            }

            gridColumn(6, maxSize = GridMaxSize.XL) {
                css {
                    +"order-1"
                    +"order-xl-2"
                }
                //canvas
                child(ThreeCanvasComponent) {
                    attrs {
                        this.context = props.context
                        this.obj = vision as? Solid
                        this.selected = selected
                        this.clickCallback = select
                        this.canvasCallback = {
                            canvas = it
                        }
                    }
                }
            }
            gridColumn(3, maxSize = GridMaxSize.XL) {
                css {
                    +"order-3"
                }
                container {
                    //settings
                    canvas?.let {
                        card("Canvas configuration") {
                            canvasControls(it)
                        }
                    }
                }
                container {
                    //properties
                    namecrumbs(selected, "World") { selected = it }
                    selected.let { selected ->
                        val selectedObject: Vision? = when {
                            selected == null -> null
                            selected.isEmpty() -> vision
                            else -> (vision as? VisionGroup)?.get(selected)
                        }
                        if (selectedObject != null) {
                            visionPropertyEditor(
                                selectedObject,
                                default = selectedObject.getAllProperties(),
                                key = selected
                            )
                        }
                    }
                }
            }
        }
    }
}
