package ru.mipt.npm.muon.monitor

import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import react.Props
import react.dom.attrs
import react.dom.button
import react.dom.p
import react.fc
import react.useMemo
import react.useState
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.invoke
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Colors
import space.kscience.visionforge.react.flexColumn
import space.kscience.visionforge.react.flexRow
import space.kscience.visionforge.ring.ThreeCanvasWithControls
import space.kscience.visionforge.ring.tab
import space.kscience.visionforge.solid.ambientLight
import space.kscience.visionforge.solid.invoke
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import space.kscience.visionforge.solid.three.edges
import styled.css
import styled.styledDiv
import styled.styledSpan
import kotlin.math.PI

external interface MMAppProps : Props {
    var model: Model
    var context: Context
    var selected: Name?
}

@OptIn(DelicateCoroutinesApi::class)
@JsExport
val MMApp = fc<MMAppProps>("Muon monitor") { props ->

    val mmOptions = useMemo {
        Canvas3DOptions {
            camera {
                distance = 2100.0
                latitude = PI / 6
                azimuth = PI + PI / 6
            }

        }
    }

    val root = useMemo(props.model) {
        props.model.root.apply {
            edges()
            ambientLight{
                color(Colors.white)
            }
        }
    }

    var events: Set<Event> by useState(emptySet())

    styledDiv {
        css {
            height = 100.vh - 12.pt
        }
        child(ThreeCanvasWithControls) {
            attrs {
                this.context = props.context
                this.builderOfSolid = CompletableDeferred(root)
                this.selected = props.selected
                this.options = mmOptions
                tab("Events") {
                    flexColumn {
                        flexRow {
                            button {
                                +"Next"
                                attrs {
                                    onClickFunction = {
                                        context.launch {
                                            val event = window.fetch(
                                                "http://localhost:8080/event",
                                                RequestInit("GET")
                                            ).then { response ->
                                                if (response.ok) {
                                                    response.text()
                                                } else {
                                                    error("Failed to get event")
                                                }
                                            }.then { body ->
                                                Json.decodeFromString(Event.serializer(), body)
                                            }.await()
                                            events = events + event
                                            props.model.displayEvent(event)
                                        }
                                    }
                                }
                            }
                            button {
                                +"Clear"
                                attrs {
                                    onClickFunction = {
                                        events = emptySet()
                                        props.model.reset()
                                    }
                                }
                            }
                        }
                    }
                    events.forEach { event ->
                        p {
                            styledSpan {
                                +event.id.toString()
                            }
                            +" : "
                            styledSpan {
                                css {
                                    color = Color.blue
                                }
                                +event.hits.toString()
                            }
                        }
                    }
                }
            }

        }
    }

//    var selected by useState { props.selected }
//
//    val onSelect: (Name?) -> Unit = {
//        selected = it
//    }
//

//
//    gridRow {
//        flexColumn {
//            css {
//                +"col-lg-3"
//                +"order-lg-1"
//                +"order-2"
//                padding(0.px)
//                overflowY = Overflow.auto
//                height = 100.vh
//            }
//            //tree
//            card("Object tree") {
//                css {
//                    flex(1.0, 1.0, FlexBasis.auto)
//                }
//                visionTree(root, selected, onSelect)
//            }
//        }
//        flexColumn {
//            css {
//                +"col-lg-6"
//                +"order-lg-2"
//                +"order-1"
//                height = 100.vh
//            }
//            h1("mx-auto page-header") {
//                +"Muon monitor demo"
//            }
//            //canvas
//
//            child(ThreeCanvasComponent) {
//                attrs {
//                    this.context = props.context
//                    this.solid = root
//                    this.selected = selected
//                    this.options = mmOptions
//                }
//            }
//        }
//        flexColumn {
//            css {
//                +"col-lg-3"
//                +"order-3"
//                padding(0.px)
//                height = 100.vh
//            }
//            styledDiv {
//                css {
//                    flex(0.0, 1.0, FlexBasis.zero)
//                }
//                //settings
//                card("Canvas configuration") {
//                    canvasControls(mmOptions, root)
//                }
//
//                card("Events") {
//                    button {
//                        +"Next"
//                        attrs {
//                            onClickFunction = {
//                                GlobalScope.launch {
//                                    val event = props.connection.get<Event>("http://localhost:8080/event")
//                                    props.model.displayEvent(event)
//                                }
//                            }
//                        }
//                    }
//                    button {
//                        +"Clear"
//                        attrs {
//                            onClickFunction = {
//                                props.model.reset()
//                            }
//                        }
//                    }
//                }
//            }
//            styledDiv {
//                css {
//                    padding(0.px)
//                }
//                nav {
//                    attrs {
//                        attributes["aria-label"] = "breadcrumb"
//                    }
//                    ol("breadcrumb") {
//                        li("breadcrumb-item") {
//                            button(classes = "btn btn-link p-0") {
//                                +"World"
//                                attrs {
//                                    onClickFunction = {
//                                        selected = Name.EMPTY
//                                    }
//                                }
//                            }
//                        }
//                        if (selected != null) {
//                            val tokens = ArrayList<NameToken>(selected?.length ?: 1)
//                            selected?.tokens?.forEach { token ->
//                                tokens.add(token)
//                                val fullName = Name(tokens.toList())
//                                li("breadcrumb-item") {
//                                    button(classes = "btn btn-link p-0") {
//                                        +token.toString()
//                                        attrs {
//                                            onClickFunction = {
//                                                console.log("Selected = $fullName")
//                                                selected = fullName
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            styledDiv {
//                css {
//                    overflowY = Overflow.auto
//                }
//                //properties
//                card("Properties") {
//                    selected.let { selected ->
//                        val selectedObject: Vision? = when {
//                            selected == null -> null
//                            selected.isEmpty() -> root
//                            else -> root[selected]
//                        }
//                        if (selectedObject != null) {
//                            visionPropertyEditor(selectedObject, key = selected)
//                        }
//                    }
//                }
//            }
//        }
//
//    }
}