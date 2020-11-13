import hep.dataforge.context.Global
import hep.dataforge.js.Application
import hep.dataforge.js.startApplication
import hep.dataforge.meta.invoke
import hep.dataforge.vision.bootstrap.visionPropertyEditor
import hep.dataforge.vision.react.ThreeCanvasComponent
import hep.dataforge.vision.react.objectTree
import hep.dataforge.vision.solid.*
import hep.dataforge.vision.solid.specifications.Canvas3DOptions
import hep.dataforge.vision.solid.three.ThreePlugin
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import react.RBuilder
import react.child
import react.dom.div
import react.dom.render

public fun RBuilder.threeCanvas(object3D: Solid, options: Canvas3DOptions.() -> Unit = {}) {
    child(ThreeCanvasComponent) {
        attrs {
            this.obj = object3D
            this.options = Canvas3DOptions(options)
        }
    }
}

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
                    visionPropertyEditor(obj)
                }
            }
        }
    }

}

fun main() {
    startApplication(::PlayGroundApp)
}