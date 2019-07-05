package hep.dataforge.vis.spatial.jsroot

import hep.dataforge.context.Global
import hep.dataforge.meta.EmptyMeta
import hep.dataforge.vis.spatial.three.ThreeOutput
import hep.dataforge.vis.spatial.demo.ApplicationBase
import hep.dataforge.vis.spatial.render
import hep.dataforge.vis.spatial.three.ThreePlugin
import hep.dataforge.vis.spatial.three.output
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import org.w3c.files.FileList
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.browser.document
import kotlin.dom.clear


class JSRootDemoApp : ApplicationBase() {

    override val stateKeys: List<String> = emptyList()

    override fun start(state: Map<String, Any>) {


        //TODO remove after DI fix
//        Global.plugins.load(ThreePlugin())
//        Global.plugins.load(JSRootPlugin())

        Global.plugins.load(JSRootPlugin)



        (document.getElementById("drop_zone") as? HTMLDivElement)?.apply {
            addEventListener("dragover", { handleDragOver(it) }, false)
            addEventListener("drop", { loadData(it) }, false)
        }


    }

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
    private fun loadData(event: Event) {
        event.stopPropagation()
        event.preventDefault()


        val file = (event.asDynamic().dataTransfer.files as FileList)[0]
            ?: throw RuntimeException("Failed to load file");
        FileReader().apply {
            onload = {
                val string = result as String
                val renderer = Global.plugins.fetch(ThreePlugin).output()
                val canvas = document.getElementById("canvas")!!
                canvas.clear()
                renderer.attach(canvas)
                println("started")

                renderer.render {
                    val json = parse(string)
                    JSRootObject(this, EmptyMeta, json).also { add(it) }
                }
            }
            readAsText(file)
        }
    }

    override fun dispose() = emptyMap<String, Any>()//mapOf("lines" to presenter.dispose())
}