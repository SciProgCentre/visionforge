package space.kscience.visionforge.gdml.demo

import javafx.geometry.Orientation
import javafx.scene.Parent
import javafx.stage.FileChooser
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.fetch
import space.kscience.gdml.GdmlShowCase
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.computeProperties
import space.kscience.visionforge.editor.VisionEditorFragment
import space.kscience.visionforge.editor.VisionTreeFragment
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.solid.FX3DPlugin
import space.kscience.visionforge.solid.FXCanvas3D
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.SolidMaterial
import tornadofx.*

class GDMLDemoApp : App(GDMLView::class)

class GDMLView : View() {
    private val context = Context {
        plugin(FX3DPlugin)
        plugin(VisionManager)
    }

    private val fx3d = context.fetch(FX3DPlugin)
    private val visionManager = context.fetch(VisionManager)
    private val canvas = FXCanvas3D(fx3d)

    private val treeFragment = VisionTreeFragment().apply {
        this.itemProperty.bind(canvas.rootObjectProperty)
    }

    private val propertyEditor = VisionEditorFragment {
        it.computeProperties()
    }.apply {
        descriptorProperty.set(SolidMaterial.descriptor)
        visionProperty.bind(treeFragment.selectedProperty)
    }

    override val root: Parent = borderpane {
        top {
            buttonbar {
                button("Load GDML/json") {
                    action {
                        val file = chooseFile("Select a GDML/json file", filters = fileNameFilter).firstOrNull()
                        if (file != null) {
                            runAsync {
                                visionManager.readFile(file) as Solid
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
            GdmlShowCase.cubes().toVision()
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