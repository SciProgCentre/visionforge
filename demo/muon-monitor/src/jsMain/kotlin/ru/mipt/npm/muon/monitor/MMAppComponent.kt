package ru.mipt.npm.muon.monitor

import hep.dataforge.context.Context
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.isEmpty
import hep.dataforge.names.length
import hep.dataforge.vision.Vision
import hep.dataforge.vision.bootstrap.canvasControls
import hep.dataforge.vision.bootstrap.card
import hep.dataforge.vision.bootstrap.gridRow
import hep.dataforge.vision.react.ThreeCanvasComponent
import hep.dataforge.vision.react.configEditor
import hep.dataforge.vision.react.flexColumn
import hep.dataforge.vision.react.objectTree
import hep.dataforge.vision.solid.specifications.Camera
import hep.dataforge.vision.solid.specifications.Canvas3DOptions
import hep.dataforge.vision.solid.three.ThreeCanvas
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import react.RProps
import react.child
import react.dom.*
import react.functionalComponent
import react.useState
import styled.css
import styled.styledDiv
import kotlin.math.PI

external interface MMAppProps : RProps {
    var model: Model
    var context: Context
    var connection: HttpClient
    var selected: Name?
}

private val canvasConfig = Canvas3DOptions {
    camera = Camera {
        distance = 2100.0
        latitude = PI / 6
        azimuth = PI + PI / 6
    }
}

@JsExport
val MMApp = functionalComponent<MMAppProps>("Muon monitor") { props ->
    var selected by useState { props.selected }
    var canvas: ThreeCanvas? by useState { null }

    val onSelect: (Name?) -> Unit = {
        selected = it
    }

    val root = props.model.root

    gridRow{
        flexColumn {
            css {
                +"col-lg-3"
                +"order-lg-1"
                +"order-2"
                padding(0.px)
                overflowY = Overflow.auto
                height = 100.vh
            }
            //tree
            card("Object tree") {
                css {
                    flex(1.0, 1.0, FlexBasis.auto)
                }
                objectTree(root, selected, onSelect)
            }
        }
        flexColumn {
            css {
                +"col-lg-6"
                +"order-lg-2"
                +"order-1"
                height = 100.vh
            }
            h1("mx-auto page-header") {
                +"Muon monitor demo"
            }
            //canvas

            child(ThreeCanvasComponent) {
                attrs {
                    this.context = props.context
                    this.obj = root
                    this.selected = selected
                    this.options = canvasConfig.apply {
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
                +"order-3"
                padding(0.px)
                height = 100.vh
            }
            styledDiv {
                css{
                    flex(0.0,1.0, FlexBasis.zero)
                }
                //settings
                canvas?.let {
                    card("Canvas configuration") {
                        canvasControls(it)
                    }
                }
                card("Events") {
                    button {
                        +"Next"
                        attrs {
                            onClickFunction = {
                                GlobalScope.launch {
                                    val event = props.connection.get<Event>("http://localhost:8080/event")
                                    props.model.displayEvent(event)
                                }
                            }
                        }
                    }
                    button {
                        +"Clear"
                        attrs {
                            onClickFunction = {
                                props.model.reset()
                            }
                        }
                    }
                }
            }
            styledDiv{
                css{
                    padding(0.px)
                }
                nav {
                    attrs {
                        attributes["aria-label"] = "breadcrumb"
                    }
                    ol("breadcrumb") {
                        li("breadcrumb-item") {
                            button(classes = "btn btn-link p-0") {
                                +"World"
                                attrs {
                                    onClickFunction = {
                                        selected = hep.dataforge.names.Name.EMPTY
                                    }
                                }
                            }
                        }
                        if (selected != null) {
                            val tokens = ArrayList<NameToken>(selected?.length ?: 1)
                            selected?.tokens?.forEach { token ->
                                tokens.add(token)
                                val fullName = Name(tokens.toList())
                                li("breadcrumb-item") {
                                    button(classes = "btn btn-link p-0") {
                                        +token.toString()
                                        attrs {
                                            onClickFunction = {
                                                console.log("Selected = $fullName")
                                                selected = fullName
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
           styledDiv{
               css{
                   overflowY = Overflow.auto
               }
                //properties
                card("Properties") {
                    selected.let { selected ->
                        val selectedObject: Vision? = when {
                            selected == null -> null
                            selected.isEmpty() -> root
                            else -> root[selected]
                        }
                        if (selectedObject != null) {
                            configEditor(
                                selectedObject.config,
                                selectedObject.descriptor,
                                default = selectedObject.allProperties,
                                key = selected
                            )
                        }
                    }
                }
            }
        }

    }
}