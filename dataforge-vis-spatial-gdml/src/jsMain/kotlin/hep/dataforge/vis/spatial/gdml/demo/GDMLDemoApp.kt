package hep.dataforge.vis.spatial.gdml.demo

import hep.dataforge.context.Global
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.hmr.ApplicationBase
import hep.dataforge.vis.hmr.startApplication
import hep.dataforge.vis.spatial.Visual3DPlugin
import hep.dataforge.vis.spatial.VisualGroup3D
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.attachChildren
import hep.dataforge.vis.spatial.gdml.GDMLTransformer
import hep.dataforge.vis.spatial.gdml.LUnit
import hep.dataforge.vis.spatial.gdml.toVisual
import hep.dataforge.vis.spatial.three.ThreeOutput
import hep.dataforge.vis.spatial.three.ThreePlugin
import hep.dataforge.vis.spatial.three.output
import hep.dataforge.vis.spatial.tree.propertyEditor
import hep.dataforge.vis.spatial.tree.render
import hep.dataforge.vis.spatial.tree.toTree
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.js.input
import kotlinx.html.js.li
import kotlinx.html.js.p
import kotlinx.html.js.ul
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
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
    private fun handleDragOver(event: Event) {
        event.stopPropagation()
        event.preventDefault()
        event.asDynamic().dataTransfer.dropEffect = "copy"
    }

    /**
     * Load data from text file
     */
    private fun loadData(event: Event, block: (name: String, data: String) -> Unit) {
        event.stopPropagation()
        event.preventDefault()


        val file = (event.asDynamic().dataTransfer.files as FileList)[0]
            ?: throw RuntimeException("Failed to load file")

        FileReader().apply {
            onload = {
                val string = result as String

//                try {
                    block(file.name, string)
//                } catch (ex: Exception) {
//                    console.error(ex)
//                }

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

    fun setupLayers(element: Element, output: ThreeOutput) {
        element.clear()
        element.append {
            ul("list-group") {
                (0..9).forEach { layer ->
                    li("list-group-item") {
                        +"layer $layer"
                        input(type = InputType.checkBox).apply {
                            if (layer == 0) {
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
    }

    private val gdmlConfiguration: GDMLTransformer.() -> Unit = {
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

        solidConfiguration = { parent, solid ->
            if (parent.physVolumes.isNotEmpty()
                || solid.name.startsWith("Coil")
                || solid.name.startsWith("Yoke")
                || solid.name.startsWith("Magnet")
                || solid.name.startsWith("Pole")
            ) {
                useStyle("opaque") {
                    VisualObject3D.OPACITY_KEY to 0.3
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

            message("Rendering")
            val output = three.output(canvas as HTMLElement)

            //output.camera.layers.enable(1)
            output.camera.layers.set(0)
            setupLayers(layers, output)

            if (visual is VisualGroup) {
                visual.toTree(editor::propertyEditor).render(tree as HTMLElement) {
                    showCheckboxes = false
                }
            }

            output.render(visual)
            message(null)
            spinner(false)
        }

        (document.getElementById("drop_zone") as? HTMLDivElement)?.apply {
            addEventListener("dragover", { handleDragOver(it) }, false)
            addEventListener("drop", { loadData(it, action) }, false)
        }

    }

    override fun dispose(): Map<String, Any> {
        return super.dispose()
    }
}

fun main() {
    startApplication(::GDMLDemoApp)
}