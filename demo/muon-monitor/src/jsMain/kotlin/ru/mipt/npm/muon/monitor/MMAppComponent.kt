package ru.mipt.npm.muon.monitor

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.isEmpty
import space.kscience.dataforge.names.length
import space.kscience.visionforge.Vision
import space.kscience.visionforge.bootstrap.canvasControls
import space.kscience.visionforge.bootstrap.card
import space.kscience.visionforge.bootstrap.gridRow
import space.kscience.visionforge.bootstrap.visionPropertyEditor
import space.kscience.visionforge.react.ThreeCanvasComponent
import space.kscience.visionforge.react.flexColumn
import space.kscience.visionforge.react.objectTree
import space.kscience.visionforge.solid.specifications.Camera
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import styled.css
import styled.styledDiv
import kotlin.math.PI

external interface MMAppProps : RProps {
    var model: Model
    var context: Context
    var connection: HttpClient
    var selected: Name?
}

@JsExport
val MMApp = functionalComponent<MMAppProps>("Muon monitor") { props ->
    var selected by useState { props.selected }

    val onSelect: (Name?) -> Unit = {
        selected = it
    }

    val mmOptions = useMemo {
        Canvas3DOptions {
            camera = Camera {
                distance = 2100.0
                latitude = PI / 6
                azimuth = PI + PI / 6
            }
            this.onSelect = onSelect
        }
    }

    val root = props.model.root

    gridRow {
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
                    this.solid = root
                    this.selected = selected
                    this.options = mmOptions
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
                css {
                    flex(0.0, 1.0, FlexBasis.zero)
                }
                //settings
                card("Canvas configuration") {
                    canvasControls(mmOptions, root)
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
            styledDiv {
                css {
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
                                        selected = Name.EMPTY
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
            styledDiv {
                css {
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
                            visionPropertyEditor(selectedObject, key = selected)
                        }
                    }
                }
            }
        }

    }
}