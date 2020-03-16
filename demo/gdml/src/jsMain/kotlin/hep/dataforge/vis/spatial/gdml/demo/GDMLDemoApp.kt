package hep.dataforge.vis.spatial.gdml.demo

import hep.dataforge.context.Global
import hep.dataforge.js.Application
import hep.dataforge.js.startApplication
import hep.dataforge.meta.Meta
import hep.dataforge.meta.withBottom
import hep.dataforge.names.Name
import hep.dataforge.names.isEmpty
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.js.editor.displayObjectTree
import hep.dataforge.vis.js.editor.displayPropertyEditor
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_COLOR_KEY
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_OPACITY_KEY
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_WIREFRAME_KEY
import hep.dataforge.vis.spatial.VisualGroup3D
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.VisualObject3D.Companion.VISIBLE_KEY
import hep.dataforge.vis.spatial.gdml.GDMLTransformer
import hep.dataforge.vis.spatial.gdml.LUnit
import hep.dataforge.vis.spatial.gdml.toVisual
import hep.dataforge.vis.spatial.three.ThreePlugin
import hep.dataforge.vis.spatial.three.displayCanvasControls
import hep.dataforge.vis.spatial.three.output
import hep.dataforge.vis.spatial.visible
import org.w3c.dom.*
import org.w3c.files.FileList
import org.w3c.files.FileReader
import org.w3c.files.get
import scientifik.gdml.GDML
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.clear

private class GDMLDemoApp : Application {
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
        console.log(message)
//        document.getElementById("messages")?.let { element ->
//            if (message == null) {
//                element.clear()
//            } else {
//                element.append {
//                    p {
//                        +message
//                    }
//                }
//            }
//        }
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
                    MATERIAL_OPACITY_KEY put 0.3
                }
            }
        }
    }


    override fun start(state: Map<String, Any>) {

        val context = Global.context("demo") {}
        val three = context.plugins.load(ThreePlugin)
        //val url = URL("https://drive.google.com/open?id=1w5e7fILMN83JGgB8WANJUYm8OW2s0WVO")

        val canvasElement = document.getElementById("canvas") ?: error("Element with id 'canvas' not found on page")
        val configElement = document.getElementById("config") ?: error("Element with id 'layers' not found on page")
        val treeElement = document.getElementById("tree") ?: error("Element with id 'tree' not found on page")
        val editorElement = document.getElementById("editor") ?: error("Element with id 'editor' not found on page")
        canvasElement.clear()

        val action: (name: String, data: String) -> Unit = { name, data ->
            canvasElement.clear()
            spinner(true)
            val visual: VisualObject3D = when {
                name.endsWith(".gdml") || name.endsWith(".xml") -> {
                    message("Loading GDML")
                    val gdml = GDML.format.parse(GDML.serializer(), data)
                    message("Converting GDML into DF-VIS format")
                    gdml.toVisual(gdmlConfiguration)
                }
                name.endsWith(".json") -> VisualGroup3D.fromJson(data)
                else -> {
                    window.alert("File extension is not recognized: $name")
                    error("File extension is not recognized: $name")
                }
            }

            //Optimize tree
            //(visual as? VisualGroup3D)?.transformInPlace(UnRef, RemoveSingleChild)

            message("Rendering")

            //output.camera.layers.enable(1)
            val canvas = three.output(canvasElement as HTMLElement)

            canvas.camera.layers.set(0)
            configElement.displayCanvasControls(canvas)
            //tree.visualObjectTree(visual, editor::propertyEditor)
            fun selectElement(name: Name) {
                val child: VisualObject = when {
                    name.isEmpty() -> visual
                    visual is VisualGroup -> visual[name] ?: return
                    else -> return
                }
                editorElement.displayPropertyEditor(name, child) { item ->
                    //val descriptorMeta = Material3D.descriptor

                    val properties = item.allProperties()
                    val bottom = Meta {
                        VISIBLE_KEY put (item.visible ?: true)
                        if (item is VisualObject3D) {
                            MATERIAL_COLOR_KEY put "#ffffff"
                            MATERIAL_OPACITY_KEY put 1.0
                            MATERIAL_WIREFRAME_KEY put false
                        }
                    }
                    properties.withBottom(bottom)
                }
            }

//        canvas.clickListener = ::selectElement

            //tree.visualObjectTree(visual, editor::propertyEditor)
            treeElement.displayObjectTree(visual) { treeName ->
                selectElement(treeName)
                canvas.highlight(treeName)
            }
            canvas.render(visual)
            message(null)
            spinner(false)
        }

        (document.getElementById("drop_zone") as? HTMLDivElement)?.apply {
            addEventListener("dragover", { handleDragOver(it as DragEvent) }, false)
            addEventListener("drop", { loadData(it as DragEvent, action) }, false)
        }
        (document.getElementById("file_load_button") as? HTMLInputElement)?.apply {
            addEventListener("change", {
                (it.target as HTMLInputElement).files?.asList()?.first()?.let { file ->
                    FileReader().apply {
                        onload = {
                            val string = result as String
                            action(file.name, string)
                        }
                        readAsText(file)
                    }
                }
            }, false)
        }

    }
}

fun main() {
    startApplication(::GDMLDemoApp)
}