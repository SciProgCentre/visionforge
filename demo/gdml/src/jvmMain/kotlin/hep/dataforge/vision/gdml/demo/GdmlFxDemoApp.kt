package hep.dataforge.vision.gdml.demo

import hep.dataforge.context.Global
import hep.dataforge.vision.editor.VisualObjectEditorFragment
import hep.dataforge.vision.editor.VisualObjectTreeFragment
import hep.dataforge.vision.gdml.toVision
import hep.dataforge.vision.solid.SolidManager
import hep.dataforge.vision.solid.SolidMaterial
import hep.dataforge.vision.solid.fx.FX3DPlugin
import hep.dataforge.vision.solid.fx.FXCanvas3D
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
        it.getAllProperties()
    }.apply {
        descriptorProperty.set(SolidMaterial.descriptor)
        itemProperty.bind(treeFragment.selectedProperty)
    }


    override val root: Parent = borderpane {
        top {
            buttonbar {
                button("Load GDML/json") {
                    action {
                        val file = chooseFile("Select a GDML/json file", filters = fileNameFilter).firstOrNull()
                        if(file!= null) {
                            runAsync {
                                SolidManager.readFile(file)
                            } ui {
                                canvas.render(it)
                            }
                        }
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

    init {
        runAsync {
            cubes().toVision()
        } ui {
            canvas.render(it)
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