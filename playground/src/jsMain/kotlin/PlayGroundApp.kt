import hep.dataforge.context.Global
import hep.dataforge.js.Application
import hep.dataforge.js.startApplication
import hep.dataforge.names.Name
import hep.dataforge.vis.bootstrap.objectTree
import hep.dataforge.vis.bootstrap.visualPropertyEditor
import hep.dataforge.vis.spatial.Point3D
import hep.dataforge.vis.spatial.VisualGroup3D
import hep.dataforge.vis.spatial.box
import hep.dataforge.vis.spatial.group
import hep.dataforge.vis.spatial.three.ThreePlugin
import hep.dataforge.vis.spatial.three.threeCanvas
import org.w3c.dom.HTMLElement
import react.dom.div
import react.dom.render
import kotlin.browser.document

private class PlayGroundApp : Application {

    private val three = Global.plugins.fetch(ThreePlugin)

    override fun start(state: Map<String, Any>) {

        val element =
            document.getElementById("app") as? HTMLElement ?: error("Element with id 'canvas' not found on page")

        val obj = VisualGroup3D().apply {
            box(100, 100, 100, name = "A")
            group("B") {
                position = Point3D(120, 0, 0)
                box(100, 100, 100, name = "C")
            }
        }

        render(element) {
            div("row") {
                div("col-3") {
                    objectTree(obj)
                }
                div("col-6") {
                    threeCanvas(obj)
                }
                div("col-3") {
                    visualPropertyEditor(Name.EMPTY, obj)
                }
            }
        }
    }

}

fun main() {
    startApplication(::PlayGroundApp)
}