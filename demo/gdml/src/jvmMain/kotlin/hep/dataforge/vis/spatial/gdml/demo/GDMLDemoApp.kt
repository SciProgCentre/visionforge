package hep.dataforge.vis.spatial.gdml.demo

import hep.dataforge.context.Global
import hep.dataforge.vis.fx.editor.VisualObjectEditorFragment
import hep.dataforge.vis.fx.editor.VisualObjectTreeFragment
import hep.dataforge.vis.spatial.Material3D
import hep.dataforge.vis.spatial.fx.FX3DPlugin
import hep.dataforge.vis.spatial.fx.FXCanvas3D
import hep.dataforge.vis.spatial.gdml.LUnit
import hep.dataforge.vis.spatial.gdml.readFile
import hep.dataforge.vis.spatial.gdml.toVisual
import hep.dataforge.vis.spatial.prototype
import javafx.geometry.Orientation
import javafx.scene.Parent
import javafx.stage.FileChooser
import scientifik.gdml.GDML
import tornadofx.*

class GDMLDemoApp : App(GDMLView::class)

class GDMLView : View() {
    private val fx3d = Global.plugins.fetch(FX3DPlugin)
    private val canvas = FXCanvas3D(fx3d)

    private val treeFragment = VisualObjectTreeFragment().apply {
        this.itemProperty.bind(canvas.rootObjectProperty)
    }

    private val propertyEditor = VisualObjectEditorFragment { it.prototype.config }.apply {
        //TODO add descriptor here
        itemProperty.bind(treeFragment.selectedProperty)
    }


    override val root: Parent = borderpane {
        top {
            buttonbar {
                button("Load GDML") {
                    action {
                        val file = chooseFile("Select a GDML file", filters = gdmlFilter).firstOrNull()
                        if (file != null) {
                            val obj = GDML.readFile(file.toPath()).toVisual {
                                lUnit = LUnit.CM

                                solidConfiguration = { parent, solid ->
                                    if(solid.name == "cave"){
                                        setProperty(Material3D.MATERIAL_WIREFRAME_KEY, true)
                                    }
                                    if (parent.physVolumes.isNotEmpty()) {
                                        useStyle("opaque") {
                                            Material3D.MATERIAL_OPACITY_KEY put 0.3
                                        }
                                    }
                                }
                            }
                            canvas.render(obj)
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

    companion object {
        private val gdmlFilter = arrayOf(
            FileChooser.ExtensionFilter("GDML", "*.gdml", "*.xml")
        )
    }
}

fun main() {
    launch<GDMLDemoApp>()
}