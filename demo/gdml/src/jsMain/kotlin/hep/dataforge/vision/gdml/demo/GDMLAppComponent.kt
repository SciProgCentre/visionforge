package hep.dataforge.vision.gdml.demo

import hep.dataforge.context.Context
import hep.dataforge.names.Name
import hep.dataforge.names.isEmpty
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionGroup
import hep.dataforge.vision.bootstrap.*
import hep.dataforge.vision.gdml.toVision
import hep.dataforge.vision.react.component
import hep.dataforge.vision.react.flexColumn
import hep.dataforge.vision.react.objectTree
import hep.dataforge.vision.react.state
import hep.dataforge.vision.solid.Solid
import hep.dataforge.vision.solid.SolidGroup
import hep.dataforge.vision.solid.specifications.Camera
import hep.dataforge.vision.solid.specifications.Canvas3DOptions
import hep.dataforge.vision.solid.three.ThreeCanvas
import hep.dataforge.vision.solid.three.ThreeCanvasComponent
import hep.dataforge.vision.solid.three.canvasControls
import kotlinx.css.FlexBasis
import kotlinx.css.Overflow
import kotlinx.css.flex
import kotlinx.css.overflow
import org.w3c.files.FileReader
import org.w3c.files.get
import react.RProps
import react.dom.h1
import scientifik.gdml.GDML
import scientifik.gdml.parse
import styled.css
import styled.styledDiv
import kotlin.browser.window
import kotlin.math.PI

interface GDMLAppProps : RProps {
    var context: Context
    var rootObject: Vision?
    var selected: Name?
}

private val canvasConfig = Canvas3DOptions {
    camera = Camera {
        distance = 2100.0
        latitude = PI / 6
        azimuth = PI + PI / 6
    }
}

val GDMLApp = component<GDMLAppProps> { props ->
    var selected by state { props.selected }
    var canvas: ThreeCanvas? by state { null }
    var visual: Vision? by state { props.rootObject }

    val select: (Name?) -> Unit = {
        selected = it
    }

    fun loadData(name: String, data: String) {
        visual = when {
            name.endsWith(".gdml") || name.endsWith(".xml") -> {
                val gdml = GDML.parse(data)
                gdml.toVision(gdmlConfiguration)
            }
            name.endsWith(".json") -> SolidGroup.parseJson(data)
            else -> {
                window.alert("File extension is not recognized: $name")
                error("File extension is not recognized: $name")
            }
        }
    }

    flexColumn {
        css {
            flex(1.0, 1.0, FlexBasis.auto)
        }
        h1 { +"GDML/JSON loader demo" }
        styledDiv {
            css {
                classes.add("row")
                classes.add("p-1")
                overflow = Overflow.auto
            }
            gridColumn(3, maxSize = GridMaxSize.XL, classes = "order-2 order-xl-1") {
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
                    visual?.let {
                        objectTree(it, selected, select)
                    }
                }
            }

            gridColumn(6, maxSize = GridMaxSize.XL, classes = "order-1 order-xl-2") {
                //canvas
                (visual as? Solid)?.let { visual3D ->
                    child(ThreeCanvasComponent::class) {
                        attrs {
                            this.context = props.context
                            this.obj = visual3D
                            this.selected = selected
                            this.clickCallback = select
                            this.canvasCallback = {
                                canvas = it
                            }
                        }
                    }
                }
            }
            gridColumn(3, maxSize = GridMaxSize.XL, classes = "order-3") {
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
                            selected.isEmpty() -> visual
                            else -> (visual as? VisionGroup)?.get(selected)
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
