package hep.dataforge.vis.spatial.gdml.demo

import hep.dataforge.context.Global
import hep.dataforge.vis.ApplicationBase
import hep.dataforge.vis.spatial.Material3D.Companion.OPACITY_KEY
import hep.dataforge.vis.spatial.Visual3DPlugin
import hep.dataforge.vis.spatial.VisualGroup3D
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.attachChildren
import hep.dataforge.vis.spatial.editor.propertyEditor
import hep.dataforge.vis.spatial.editor.threeOutputConfig
import hep.dataforge.vis.spatial.editor.visualObjectTree
import hep.dataforge.vis.spatial.gdml.GDMLTransformer
import hep.dataforge.vis.spatial.gdml.LUnit
import hep.dataforge.vis.spatial.gdml.toVisual
import hep.dataforge.vis.spatial.three.ThreePlugin
import hep.dataforge.vis.spatial.three.output
import hep.dataforge.vis.startApplication
import kotlinx.html.dom.append
import kotlinx.html.js.p
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.files.FileList
import org.w3c.files.FileReader
import org.w3c.files.get
import scientifik.gdml.GDML
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.clear

private class GDMLDemoApp : ApplicationBase() {
    /**
     * Handle mouse drag according to https://www.html5rocks.com/en/tutorials/file/dndfiles/
     */
    private fun handleDragOver(event: DragEvent) {
        event.stopPropagation()
        event.preventDefault()
        event.dataTransfer?.dropEffect = "copy"
    }

    /**
     * Load data from text file
     */
    private fun loadData(event: DragEvent, block: (name: String, data: String) -> Unit) {
        event.stopPropagation()
        event.preventDefault()


        val file = (event.dataTransfer?.files as FileList)[0]
            ?: throw RuntimeException("Failed to load file")

        FileReader().apply {
            onload = {
                val string = result as String
                block(file.name, string)
            }
            readAsText(file)
        }
    }

    private fun spinner(show: Boolean) {
//        if( show){
//
//        val style = if (show) {
//            "display:block;"
//        } else {
//            "display:none;"
//        }
//        document.getElementById("canvas")?.append {
//
//        }
    }

    private fun message(message: String?) {
        document.getElementById("messages")?.let { element ->
            if (message == null) {
                element.clear()
            } else {
                element.append {
                    p {
                        +message
                    }
                }
            }
        }
    }

    private val gdmlConfiguration: GDMLTransformer.() -> Unit = {
        lUnit = LUnit.CM
        volumeAction = { volume ->
            when {
                volume.name.startsWith("ecal01lay") -> GDMLTransformer.Action.REJECT
                volume.name.startsWith("UPBL") -> GDMLTransformer.Action.REJECT
                volume.name.startsWith("USCL") -> GDMLTransformer.Action.REJECT
                volume.name.startsWith("VPBL") -> GDMLTransformer.Action.REJECT
                volume.name.startsWith("VSCL") -> GDMLTransformer.Action.REJECT
                else -> GDMLTransformer.Action.CACHE
            }
        }

        solidConfiguration = { parent, solid ->
            if (
                solid.name.startsWith("Yoke")
                || solid.name.startsWith("Pole")
                || parent.physVolumes.isNotEmpty()
            ) {
                useStyle("opaque") {
                    OPACITY_KEY to 0.3
                }
            }
        }
    }


    override fun start(state: Map<String, Any>) {

        val context = Global.context("demo") {}
        val three = context.plugins.load(ThreePlugin)
        //val url = URL("https://drive.google.com/open?id=1w5e7fILMN83JGgB8WANJUYm8OW2s0WVO")

        val canvas = document.getElementById("canvas") ?: error("Element with id 'canvas' not found on page")
        val layers = document.getElementById("layers") ?: error("Element with id 'layers' not found on page")
        val tree = document.getElementById("tree") ?: error("Element with id 'tree' not found on page")
        val editor = document.getElementById("editor") ?: error("Element with id 'editor' not found on page")
        canvas.clear()

        val action: (name: String, data: String) -> Unit = { name, data ->
            canvas.clear()
            spinner(true)
            message("Loading GDML")
            val gdml = GDML.format.parse(GDML.serializer(), data)
            message("Converting GDML into DF-VIS format")

            val visual: VisualObject3D = when {
                name.endsWith(".gdml") || name.endsWith(".xml") -> gdml.toVisual(gdmlConfiguration)
                name.endsWith(".json") -> {
                    Visual3DPlugin.json.parse(VisualGroup3D.serializer(), data).apply { attachChildren() }
                }
                else -> {
                    window.alert("File extension is not recognized: $name")
                    error("File extension is not recognized: $name")
                }
            }

            //Optimize tree
            //(visual as? VisualGroup3D)?.transformInPlace(UnRef, RemoveSingleChild)

            message("Rendering")

            //output.camera.layers.enable(1)
            val output = three.output(canvas as HTMLElement)

            output.camera.layers.set(0)
            layers.threeOutputConfig(output)
            tree.visualObjectTree(visual, editor::propertyEditor)

            output.render(visual)
            message(null)
            spinner(false)
        }

        (document.getElementById("drop_zone") as? HTMLDivElement)?.apply {
            addEventListener("dragover", { handleDragOver(it as DragEvent) }, false)
            addEventListener("drop", { loadData(it as DragEvent, action) }, false)
        }

    }

    override fun dispose(): Map<String, Any> {
        return super.dispose()
    }
}

fun main() {
    startApplication(::GDMLDemoApp)
}