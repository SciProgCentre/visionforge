package hep.dataforge.vision.spatial.gdml.demo

import hep.dataforge.context.Context
import hep.dataforge.names.Name
import hep.dataforge.names.isEmpty
import hep.dataforge.vision.VisualGroup
import hep.dataforge.vision.VisualObject
import hep.dataforge.vision.bootstrap.*
import hep.dataforge.vision.react.component
import hep.dataforge.vision.react.configEditor
import hep.dataforge.vision.react.flexColumn
import hep.dataforge.vision.react.state
import hep.dataforge.vision.spatial.VisualGroup3D
import hep.dataforge.vision.spatial.VisualObject3D
import hep.dataforge.vision.spatial.gdml.toVisual
import hep.dataforge.vision.spatial.specifications.Camera
import hep.dataforge.vision.spatial.specifications.Canvas3DOptions
import hep.dataforge.vision.spatial.three.ThreeCanvas
import hep.dataforge.vision.spatial.three.ThreeCanvasComponent
import hep.dataforge.vision.spatial.three.canvasControls
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
    var rootObject: VisualObject?
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
    var visual: VisualObject? by state { props.rootObject }

    val select: (Name?) -> Unit = {
        selected = it
    }

    fun loadData(name: String, data: String) {
        visual = when {
            name.endsWith(".gdml") || name.endsWith(".xml") -> {
                val gdml = GDML.parse(data)
                gdml.toVisual(gdmlConfiguration)
            }
            name.endsWith(".json") -> VisualGroup3D.parseJson(data)
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
            gridColumn(3, maxSize= GridMaxSize.XL, classes = "order-2 order-xl-1") {
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
                card("Object tree", "overflow-auto") {
                    visual?.let {
                        objectTree(it, selected, select)
                    }
                }
            }

            gridColumn(6, maxSize= GridMaxSize.XL, classes = "order-1 order-xl-2") {
                //canvas
                (visual as? VisualObject3D)?.let { visual3D ->
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
            gridColumn(3, maxSize= GridMaxSize.XL, classes = "order-3") {
                container {
                    //settings
                    canvas?.let {
                        card("Canvas configuration") {
                            canvasControls(it)
                        }
                    }
                }
                container {
                    namecrumbs(selected, "World") { selected = it }
                }
                container {
                    //properties
                    card("Properties") {
                        selected.let { selected ->
                            val selectedObject: VisualObject? = when {
                                selected == null -> null
                                selected.isEmpty() -> visual
                                else -> (visual as? VisualGroup)?.get(selected)
                            }
                            if (selectedObject != null) {
                                configEditor(selectedObject, default = selectedObject.properties(), key = selected)
                            }
                        }
                    }
                }
            }
        }
    }
}