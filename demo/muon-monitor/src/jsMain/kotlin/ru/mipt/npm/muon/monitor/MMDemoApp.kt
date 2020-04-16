package ru.mipt.npm.muon.monitor

import hep.dataforge.context.Global
import hep.dataforge.js.Application
import hep.dataforge.js.startApplication
import hep.dataforge.vis.spatial.Visual3D
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.serialization.json.Json
import react.dom.render
import kotlin.browser.document

private class MMDemoApp : Application {

    private val model = Model()

    private val connection = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json(context = Visual3D.serialModule))
        }
    }

    override fun start(state: Map<String, Any>) {

        val context = Global.context("demo") {}
//        val three = context.plugins.load(ThreePlugin)

        val element = document.getElementById("app") ?: error("Element with id 'app' not found on page")

        render(element) {
            child(MMAppComponent::class) {
                attrs {
                    this.model = model
                    this.context = context
                }
            }
        }
//        //val url = URL("https://drive.google.com/open?id=1w5e7fILMN83JGgB8WANJUYm8OW2s0WVO")
//
//        val canvasElement = document.getElementById("canvas") ?: error("Element with id 'canvas' not found on page")
//        val settingsElement = document.getElementById("settings")
//            ?: error("Element with id 'settings' not found on page")
//        val treeElement = document.getElementById("tree") ?: error("Element with id 'tree' not found on page")
//        val editorElement = document.getElementById("editor") ?: error("Element with id 'editor' not found on page")
//
//        canvasElement.clear()
//        val visual = model.root
//
//        //output.camera.layers.enable(1)
//        val canvas = three.output(canvasElement as HTMLElement)
//
//        canvas.camera.layers.set(0)
//        canvas.camera.position.z = -2000.0
//        canvas.camera.position.y = 500.0
//        canvas.camera.lookAt(Vector3(0, 0, 0))
//
//        settingsElement.displayCanvasControls(canvas) {
//            card("Events") {
//                button {
//                    +"Next"
//                    onClickFunction = {
//                        GlobalScope.launch {
//                            val event = connection.get<Event>("http://localhost:8080/event")
//                            model.displayEvent(event)
//                        }
//                    }
//                }
//                button {
//                    +"Clear"
//                    onClickFunction = {
//                        model.reset()
//                    }
//                }
//            }
//        }
//
//        var objectTreeContainer: ObjectTreeContainer? = null
//
//        fun selectElement(name: Name?) {
//            if (name != null) {
//                canvas.select(name)
//                val child: VisualObject = when {
//                    name.isEmpty() -> visual
//                    visual is VisualGroup -> visual[name] ?: return
//                    else -> return
//                }
//                editorElement.visualPropertyEditor(name, child, descriptor = VisualObject3D.descriptor)
//                objectTreeContainer?.select(name)
//            }
//        }
//
//        val selectElementFunction: (Name?) -> Unit = { name ->
//            selectElement(name?.selectable())
//        }
//
//        canvas.onClick = selectElementFunction
//
//        objectTreeContainer = treeElement.renderObjectTree(visual, selectElementFunction)
//        canvas.render(visual)
    }
}

fun main() {
    startApplication(::MMDemoApp)
}