import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.client.VisionClient
import hep.dataforge.vision.client.renderAllVisions
import hep.dataforge.vision.plotly.PlotlyPlugin
import hep.dataforge.vision.solid.three.ThreePlugin
import kotlinx.browser.window

//fun RBuilder.threeCanvas(object3D: Solid, options: Canvas3DOptions.() -> Unit = {}) {
//    child(ThreeCanvasComponent) {
//        attrs {
//            this.obj = object3D
//            this.options = Canvas3DOptions(options)
//        }
//    }
//}
//
//private class PlayGroundApp : Application {
//
//    override fun start(state: Map<String, Any>) {
//
//        val element =
//            document.getElementById("app") as? HTMLElement ?: error("Element with id 'canvas' not found on page")
//
//        val obj = SolidGroup().apply {
//            box(100, 100, 100, name = "A")
//            group("B") {
//                position = Point3D(120, 0, 0)
//                box(100, 100, 100, name = "C")
//            }
//        }
//
//        render(element) {
//            div("row") {
//                div("col-3") {
//                    objectTree(obj)
//                }
//                div("col-6") {
//                    threeCanvas(obj)
//                }
//                div("col-3") {
//                    visionPropertyEditor(obj)
//                }
//            }
//        }
//    }
//
//}

public val visionContext: Context = Global.context("VISION") {
    plugin(ThreePlugin)
    plugin(PlotlyPlugin)
    plugin(VisionClient)
}

@DFExperimental
fun main() {
    //Loading three-js renderer
    val clientManager = visionContext.plugins.fetch(VisionClient)

    //Fetch from server and render visions for all outputs
    window.onload = {
        clientManager.renderAllVisions()
    }
    //startApplication(::PlayGroundApp)
}