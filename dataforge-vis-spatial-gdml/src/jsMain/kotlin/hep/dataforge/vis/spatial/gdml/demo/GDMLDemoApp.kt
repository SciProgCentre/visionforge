package hep.dataforge.vis.spatial.gdml.demo

import hep.dataforge.context.Global
import hep.dataforge.vis.hmr.ApplicationBase
import hep.dataforge.vis.hmr.startApplication
import hep.dataforge.vis.spatial.gdml.GDMLTransformer
import hep.dataforge.vis.spatial.gdml.LUnit
import hep.dataforge.vis.spatial.gdml.toVisual
import hep.dataforge.vis.spatial.three.ThreePlugin
import hep.dataforge.vis.spatial.three.output
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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


    override fun start(state: Map<String, Any>) {

        val context = Global.context("demo") {}
        val three = context.plugins.load(ThreePlugin)
        //val url = URL("https://drive.google.com/open?id=1w5e7fILMN83JGgB8WANJUYm8OW2s0WVO")

        val canvas = document.getElementById("canvas") ?: error("Element with id canvas not found on page")
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
                        volume.name.startsWith("ecal") -> GDMLTransformer.Action.REJECT
                        volume.name.startsWith("U") -> GDMLTransformer.Action.CACHE
                        volume.name.startsWith("V") -> GDMLTransformer.Action.CACHE
                        else -> GDMLTransformer.Action.ACCEPT
                    }
                }

                transparent = { !physVolumes.isEmpty() || (it?.name?.startsWith("Coil") ?: false) }

            }
            launch { message("Rendering") }
            val output = three.output(canvas) {
                "axis" to {
                    "size" to 100
                }
            }
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