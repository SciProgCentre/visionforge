package hep.dataforge.vis.spatial.gdml.demo

import hep.dataforge.context.Global
import hep.dataforge.vis.hmr.ApplicationBase
import hep.dataforge.vis.hmr.startApplication
import hep.dataforge.vis.spatial.gdml.GDMLTransformer
import hep.dataforge.vis.spatial.gdml.LUnit
import hep.dataforge.vis.spatial.gdml.toVisual
import hep.dataforge.vis.spatial.opacity
import hep.dataforge.vis.spatial.three.ThreeOutput
import hep.dataforge.vis.spatial.three.ThreePlugin
import hep.dataforge.vis.spatial.three.output
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.input
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import org.w3c.files.FileList
import org.w3c.files.FileReader
import org.w3c.files.get
import scientifik.gdml.GDML
import kotlin.browser.document
import kotlin.dom.clear

private class GDMLDemoApp : ApplicationBase() {


    /**
     * Handle mouse drag according to https://www.html5rocks.com/en/tutorials/file/dndfiles/
     */
    private fun handleDragOver(event: Event) {
        event.stopPropagation()
        event.preventDefault()
        event.asDynamic().dataTransfer.dropEffect = "copy"
    }

    /**
     * Load data from text file
     */
    private fun loadData(event: Event, block: suspend CoroutineScope.(String) -> Unit) {
        event.stopPropagation()
        event.preventDefault()


        val file = (event.asDynamic().dataTransfer.files as FileList)[0]
            ?: throw RuntimeException("Failed to load file")

        FileReader().apply {
            onload = {
                val string = result as String
                GlobalScope.launch {
                    block(string)
                }
            }
            readAsText(file)
        }
    }

    private fun spinner(show: Boolean) {
        val style = if (show) {
            "display:block;"
        } else {
            "display:none;"
        }
        document.getElementById("loader")?.setAttribute("style", style)
    }

    private fun message(message: String?) {
        val element = document.getElementById("message")
        if (message == null) {
            element?.setAttribute("style", "display:none;")
        } else {
            element?.textContent = message
            element?.setAttribute("style", "display:block;")
        }
    }

    fun setupSidebar(element: Element, output: ThreeOutput) {
        element.clear()
        (0..9).forEach{layer->
            element.append {
                div("row") {
                    +"layer $layer"
                    input(type = InputType.checkBox).apply {
                        if (layer == 0 || layer == 1) {
                            checked = true
                        }
                        onchange = {
                            if (checked) {
                                output.camera.layers.enable(layer)
                            } else {
                                output.camera.layers.disable(layer)
                            }
                        }
                    }
                }
            }
        }
    }


    override fun start(state: Map<String, Any>) {

        val context = Global.context("demo") {}
        val three = context.plugins.load(ThreePlugin)
        //val url = URL("https://drive.google.com/open?id=1w5e7fILMN83JGgB8WANJUYm8OW2s0WVO")

        val canvas = document.getElementById("canvas") ?: error("Element with id 'canvas' not found on page")
        val sidebar = document.getElementById("sidebar") ?: error("Element with id 'sidebar' not found on page")
        canvas.clear()

        val action: suspend CoroutineScope.(String) -> Unit = { it ->
            canvas.clear()
            launch { spinner(true) }
            launch { message("Loading GDML") }
            val gdml = GDML.format.parse(GDML.serializer(), it)
            launch { message("Converting GDML into DF-VIS format") }
            val visual = gdml.toVisual {
                lUnit = LUnit.CM
                volumeAction = { volume ->
                    when {
                        volume.name.startsWith("ecal01lay") -> GDMLTransformer.Action.REJECT
                        volume.name.startsWith("ecal") -> GDMLTransformer.Action.CACHE
                        volume.name.startsWith("UPBL") -> GDMLTransformer.Action.REJECT
                        volume.name.startsWith("USCL") -> GDMLTransformer.Action.REJECT
                        volume.name.startsWith("U") -> GDMLTransformer.Action.CACHE
                        volume.name.startsWith("VPBL") -> GDMLTransformer.Action.REJECT
                        volume.name.startsWith("VSCL") -> GDMLTransformer.Action.REJECT
                        volume.name.startsWith("V") -> GDMLTransformer.Action.CACHE
                        else -> GDMLTransformer.Action.ACCEPT
                    }
                }

                configure = { parent, solid ->
                    if (!parent.physVolumes.isEmpty()) {
                        opacity = 0.3
                    }
                    if (solid.name.startsWith("Coil")
                        || solid.name.startsWith("Yoke")
                        || solid.name.startsWith("Magnet")
                        || solid.name.startsWith("Pole")
                    ) {
                        opacity = 0.3
                    }
                }
            }
            launch { message("Rendering") }
            val output = three.output(canvas) {
                //                "axis" to {
//                    "size" to 100
//                }
            }
            //make top layer visible
            //output.camera.layers.disable(0)
            output.camera.layers.enable(1)

            setupSidebar(sidebar, output)

            output.render(visual)
            launch {
                message(null)
                spinner(false)
            }
        }

        (document.getElementById("drop_zone") as? HTMLDivElement)?.apply {
            addEventListener("dragover", { handleDragOver(it) }, false)
            addEventListener("drop", { loadData(it, action) }, false)
        }

    }

}

fun main() {
    startApplication(::GDMLDemoApp)
}