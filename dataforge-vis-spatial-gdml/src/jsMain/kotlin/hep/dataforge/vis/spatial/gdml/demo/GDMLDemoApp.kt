package hep.dataforge.vis.spatial.gdml.demo

import hep.dataforge.context.Global
import hep.dataforge.vis.hmr.ApplicationBase
import hep.dataforge.vis.hmr.startApplication
import hep.dataforge.vis.spatial.gdml.toVisual
import hep.dataforge.vis.spatial.three.ThreePlugin
import hep.dataforge.vis.spatial.three.output
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
    private fun loadData(event: Event, block: suspend (String) -> Unit) {
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


    override fun start(state: Map<String, Any>) {

        val context = Global.context("demo") {}
        val three = context.plugins.load(ThreePlugin)
        //val url = URL("https://drive.google.com/open?id=1w5e7fILMN83JGgB8WANJUYm8OW2s0WVO")

        val canvas = document.getElementById("canvas") ?: error("Element with id canvas not found on page")

        val action: suspend (String) -> Unit = {
            canvas.clear()
            val output = three.output(canvas)
            val gdml = GDML.format.parse(GDML.serializer(), it)
            val visual = gdml.toVisual()
            output.render(visual)
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