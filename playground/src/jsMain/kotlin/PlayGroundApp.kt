import hep.dataforge.context.Global
import hep.dataforge.js.Application
import hep.dataforge.js.startApplication
import hep.dataforge.names.Name
import hep.dataforge.vision.bootstrap.visualPropertyEditor
import hep.dataforge.vision.react.objectTree
import hep.dataforge.vision.solid.Point3D
import hep.dataforge.vision.solid.SolidGroup
import hep.dataforge.vision.solid.box
import hep.dataforge.vision.solid.group
import hep.dataforge.vision.solid.three.ThreePlugin
import hep.dataforge.vision.solid.three.threeCanvas
import org.w3c.dom.HTMLElement
import react.dom.div
import react.dom.render
import kotlin.browser.document

private class PlayGroundApp : Application {

    private val three = Global.plugins.fetch(ThreePlugin)

    override fun start(state: Map<String, Any>) {

        val element =
            document.getElementById("app") as? HTMLElement ?: error("Element with id 'canvas' not found on page")

        val obj = SolidGroup().apply {
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