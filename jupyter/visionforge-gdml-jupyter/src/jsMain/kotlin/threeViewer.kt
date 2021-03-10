package space.kscience.visionforge.gdml.jupyter

import kotlinx.css.*
import react.RProps
import react.child
import react.dom.h1
import react.functionalComponent
import react.useState
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.bootstrap.gridRow
import space.kscience.visionforge.bootstrap.nameCrumbs
import space.kscience.visionforge.bootstrap.threeControls
import space.kscience.visionforge.react.ThreeCanvasComponent
import space.kscience.visionforge.react.flexColumn
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import space.kscience.visionforge.solid.three.ThreeCanvas
import styled.css
import styled.styledDiv

external interface GdmlViewProps : RProps {
    var context: Context
    var rootVision: Vision?
    var selected: Name?
}

@JsExport
val GdmlView = functionalComponent<GdmlViewProps>("GdmlView") { props ->
    var selected by useState { props.selected }
    var canvas: ThreeCanvas? by useState { null }
    var vision: Vision? by useState { props.rootVision }

    val onSelect: (Name?) -> Unit = {
        selected = it
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
            canvas?.let {
                threeControls(it, selected, onSelect)
            }
        }
    }
}