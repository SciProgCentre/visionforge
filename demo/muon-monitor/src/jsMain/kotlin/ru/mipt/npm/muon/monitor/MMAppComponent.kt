package ru.mipt.npm.muon.monitor

import hep.dataforge.context.Context
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.isEmpty
import hep.dataforge.names.length
import hep.dataforge.vision.Vision
import hep.dataforge.vision.bootstrap.canvasControls
import hep.dataforge.vision.bootstrap.card
import hep.dataforge.vision.react.component
import hep.dataforge.vision.react.configEditor
import hep.dataforge.vision.react.objectTree
import hep.dataforge.vision.solid.specifications.Camera
import hep.dataforge.vision.solid.specifications.Canvas3DOptions
import hep.dataforge.vision.solid.three.ThreeCanvas
import hep.dataforge.vision.solid.three.ThreeCanvasComponent
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import react.RProps
import react.dom.*
import react.getValue
import react.setValue
import react.useState
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
val MMApp = component<MMAppProps> { props ->
    var selected by useState  { props.selected }
    var canvas: ThreeCanvas? by useState { null }

    val select: (Name?) -> Unit = {
        selected = it
    }

    val root = props.model.root

    div("row") {
        h1("mx-auto") {
            +"Muon monitor demo"
        }
    }
    div("row") {
        div("col-lg-3 px-0 overflow-auto") {
            //tree
            card("Object tree") {
                objectTree(root, selected, select)
            }
        }
        div("col-lg-6") {
            //canvas
            child(ThreeCanvasComponent::class) {
                attrs {
                    this.context = props.context
                    this.obj = root
                    this.options = canvasConfig
                    this.selected = selected
                    this.clickCallback = select
                    this.canvasCallback = {
                        canvas = it
                    }
                }
            }
        }
        div("col-lg-3") {
            div("row") {
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
            div("row") {
                div("container-fluid p-0") {
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
            }
            div("row") {
                //properties
                card("Properties") {
                    selected.let { selected ->
                        val selectedObject: Vision? = when {
                            selected == null -> null
                            selected.isEmpty() -> root
                            else -> root[selected]
                        }
                        if (selectedObject != null) {
                            configEditor(selectedObject, default = selectedObject.getAllProperties(), key = selected)
                        }
                    }
                }
            }
        }

    }
}