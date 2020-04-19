package hep.dataforge.vis.spatial.gdml.demo

import hep.dataforge.context.Global
import hep.dataforge.vis.editor.VisualObjectEditorFragment
import hep.dataforge.vis.editor.VisualObjectTreeFragment
import hep.dataforge.vis.spatial.Material3D
import hep.dataforge.vis.spatial.Visual3D
import hep.dataforge.vis.spatial.VisualGroup3D
import hep.dataforge.vis.spatial.fx.FX3DPlugin
import hep.dataforge.vis.spatial.fx.FXCanvas3D
import javafx.geometry.Orientation
import javafx.scene.Parent
import javafx.stage.FileChooser
import tornadofx.*

class GDMLDemoApp : App(GDMLView::class)

class GDMLView : View() {
    private val fx3d = Global.plugins.fetch(FX3DPlugin)
    private val canvas = FXCanvas3D(fx3d)

    private val treeFragment = VisualObjectTreeFragment().apply {
        this.itemProperty.bind(canvas.rootObjectProperty)
    }

    private val propertyEditor = VisualObjectEditorFragment {
        it.allProperties()
    }.apply {
        descriptorProperty.set(Material3D.descriptor)
        itemProperty.bind(treeFragment.selectedProperty)
    }


    override val root: Parent = borderpane {
        top {
            buttonbar {
                button("Load GDML/json") {
                    action {
                        val file = chooseFile("Select a GDML/json file", filters = fileNameFilter).firstOrNull()
                            ?: return@action
                        val visual: VisualGroup3D = Visual3D.readFile(file)
                        canvas.render(visual)
                    }
                }
            }
        }
        center {
            splitpane(Orientation.HORIZONTAL, treeFragment.root, canvas.root, propertyEditor.root) {
                setDividerPositions(0.2, 0.6, 0.2)
            }
        }
    }

    companion object {
        private val fileNameFilter = arrayOf(
            FileChooser.ExtensionFilter("GDML", "*.gdml", "*.xml"),
            FileChooser.ExtensionFilter("JSON", "*.json"),
            FileChooser.ExtensionFilter("JSON.ZIP", "*.json.zip"),
            FileChooser.ExtensionFilter("JSON.GZ", "*.json.gz")
        )
    }
}

fun main() {
    launch<GDMLDemoApp>()
}