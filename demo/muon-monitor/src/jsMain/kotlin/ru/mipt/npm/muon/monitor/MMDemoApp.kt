package ru.mipt.npm.muon.monitor

import hep.dataforge.context.Global
import hep.dataforge.js.Application
import hep.dataforge.js.startApplication
import hep.dataforge.meta.buildMeta
import hep.dataforge.meta.withBottom
import hep.dataforge.names.NameToken
import hep.dataforge.vis.js.editor.objectTree
import hep.dataforge.vis.js.editor.propertyEditor
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_COLOR_KEY
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_OPACITY_KEY
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_WIREFRAME_KEY
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.VisualObject3D.Companion.VISIBLE_KEY
import hep.dataforge.vis.spatial.three.ThreePlugin
import hep.dataforge.vis.spatial.three.output
import hep.dataforge.vis.spatial.three.threeSettings
import hep.dataforge.vis.spatial.visible
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.dom.clear
import kotlin.math.PI

private class GDMLDemoApp : Application {
//    /**
//     * Handle mouse drag according to https://www.html5rocks.com/en/tutorials/file/dndfiles/
//     */
//    private fun handleDragOver(event: DragEvent) {
//        event.stopPropagation()
//        event.preventDefault()
//        event.dataTransfer?.dropEffect = "copy"
//    }
//
//    /**
//     * Load data from text file
//     */
//    private fun loadData(event: DragEvent, block: (name: String, data: String) -> Unit) {
//        event.stopPropagation()
//        event.preventDefault()
//
//        val file = (event.dataTransfer?.files as FileList)[0]
//            ?: throw RuntimeException("Failed to load file")
//
//        FileReader().apply {
//            onload = {
//                val string = result as String
//                block(file.name, string)
//            }
//            readAsText(file)
//        }
//    }

    private val model = Model()

    override fun start(state: Map<String, Any>) {

        val context = Global.context("demo") {}
        val three = context.plugins.load(ThreePlugin)
        //val url = URL("https://drive.google.com/open?id=1w5e7fILMN83JGgB8WANJUYm8OW2s0WVO")

        val canvasElement = document.getElementById("canvas") ?: error("Element with id 'canvas' not found on page")
        val settingsElement =
            document.getElementById("settings") ?: error("Element with id 'settings' not found on page")
        val treeElement = document.getElementById("tree") ?: error("Element with id 'tree' not found on page")
        val editorElement = document.getElementById("editor") ?: error("Element with id 'editor' not found on page")
        canvasElement.clear()

        canvasElement.clear()
        val visual: VisualObject3D = model.root

        //output.camera.layers.enable(1)
        val output = three.output(canvasElement as HTMLElement)

        output.camera.layers.set(0)
        output.camera.position.z = -2000.0
        output.camera.position.y = 500.0
        settingsElement.threeSettings(output)
        //tree.visualObjectTree(visual, editor::propertyEditor)
        treeElement.objectTree(NameToken("World"), visual) {
            editorElement.propertyEditor(it) { item ->
                //val descriptorMeta = Material3D.descriptor

                val properties = item.allProperties()
                val bottom = buildMeta {
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


        output.render(visual)

    }
}

fun main() {
    startApplication(::GDMLDemoApp)
}