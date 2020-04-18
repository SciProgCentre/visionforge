package ru.mipt.npm.muon.monitor

import hep.dataforge.context.Context
import hep.dataforge.js.card
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.isEmpty
import hep.dataforge.vis.VisualObject
import hep.dataforge.vis.editor.configEditor
import hep.dataforge.vis.editor.objectTree
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.specifications.Camera
import hep.dataforge.vis.spatial.specifications.Canvas
import hep.dataforge.vis.spatial.three.ThreeCanvas
import hep.dataforge.vis.spatial.three.ThreeCanvasComponent
import hep.dataforge.vis.spatial.three.canvasControls
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.*
import kotlin.math.PI

interface MMAppProps : RProps {
    var model: Model
    var context: Context
    var connection: HttpClient
}

interface MMAppState : RState {
    var selected: Name?
    var canvas: ThreeCanvas?
}

class MMAppComponent : RComponent<MMAppProps, MMAppState>() {

    private val onSelect: (Name?) -> Unit = {
        setState {
            selected = it
        }
    }

    private val canvasConfig = Canvas {
        camera = Camera {
            distance = 2100.0
            latitude = PI / 6
            azimuth = PI + PI / 6
        }
    }

    override fun RBuilder.render() {
        val visual = props.model.root
        val selected = state.selected

        div("row") {
            div("col-lg-3") {
                //tree
                card("Object tree") {
                    objectTree(visual, selected, onSelect)
                }
            }
            div("col-lg-6") {
                //canvas
                child(ThreeCanvasComponent::class) {
                    attrs {
                        this.context = props.context
                        this.obj = visual
                        this.options = canvasConfig
                        this.selected = selected
                        this.clickCallback = onSelect
                        this.canvasCallback = {
                            setState {
                                canvas = it
                            }
                        }
                    }
                }
            }
            div("col-lg-3") {
                div("row") {
                    //settings
                    state.canvas?.let {
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
                                                setState {
                                                    this.selected = Name.EMPTY
                                                }
                                            }
                                        }
                                    }
                                }
                                if (selected != null) {
                                    val tokens = ArrayList<NameToken>(selected.length)
                                    selected.tokens.forEach { token ->
                                        tokens.add(token)
                                        val fullName = Name(tokens.toList())
                                        li("breadcrumb-item") {
                                            button(classes = "btn btn-link p-0") {
                                                +token.toString()
                                                attrs {
                                                    onClickFunction = {
                                                        setState {
                                                            console.log("Selected = $fullName")
                                                            this.selected = fullName
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
                }
                div("row") {
                    //properties
                    if (selected != null) {
                        val selectedObject: VisualObject? = when {
                            selected.isEmpty() -> visual
                            else -> visual[selected]
                        }
                        if (selectedObject != null) {
                            card("Properties") {
                                configEditor(selectedObject, descriptor = VisualObject3D.descriptor)
                            }
                        }
                    }
                }
            }
        }
    }
}