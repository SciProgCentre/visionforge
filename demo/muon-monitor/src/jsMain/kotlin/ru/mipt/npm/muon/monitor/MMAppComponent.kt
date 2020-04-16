package ru.mipt.npm.muon.monitor

import hep.dataforge.context.Context
import hep.dataforge.js.card
import hep.dataforge.names.Name
import hep.dataforge.names.isEmpty
import hep.dataforge.vis.editor.objectTree
import hep.dataforge.vis.editor.visualPropertyEditor
import hep.dataforge.vis.spatial.Visual3D
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.specifications.Camera
import hep.dataforge.vis.spatial.specifications.Canvas
import hep.dataforge.vis.spatial.three.ThreeCanvas
import hep.dataforge.vis.spatial.three.ThreeCanvasComponent
import hep.dataforge.vis.spatial.three.canvasControls
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.json.Json
import react.*
import react.dom.button
import react.dom.div
import kotlin.math.PI

interface MMAppProps : RProps {
    var model: Model
    var context: Context
}

interface MMAppState : RState {
    var model: Model
    var selected: Name?
    var canvas: ThreeCanvas?
}

class MMAppComponent : RComponent<MMAppProps, MMAppState>() {

    private val model = Model()

    private val connection = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json(context = Visual3D.serialModule))
        }
    }

    override fun MMAppState.init(props: MMAppProps) {
        this.model = props.model
    }

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
        val visual = model.root
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
                           setState{
                               canvas = it
                           }
                        }
                    }
                }
            }
            div("col-lg-3") {
                div("row") {
                    //settings
                    state.canvas?.let { canvasControls(it) }
                    card("Events") {
                        button {
                            +"Next"
                            attrs {
                                onClickFunction = {
                                    GlobalScope.launch {
                                        val event = connection.get<Event>("http://localhost:8080/event")
                                        model.displayEvent(event)
                                    }
                                }
                            }
                        }
                        button {
                            +"Clear"
                            attrs {
                                onClickFunction = {
                                    model.reset()
                                }
                            }
                        }
                    }
                }
                div("row") {
                    //properties
                    if (selected != null) {
                        val selectedObject = when {
                            selected.isEmpty() -> visual
                            else -> visual[selected]
                        }
                        if (selectedObject != null) {
                            //TODO replace by explicit breadcrumbs with callback
                            visualPropertyEditor(selected, selectedObject, descriptor = VisualObject3D.descriptor)
                        }
                    }
                }
            }
        }
    }
}