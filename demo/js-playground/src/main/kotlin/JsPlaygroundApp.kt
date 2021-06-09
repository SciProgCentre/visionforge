import kotlinx.browser.document
import kotlinx.css.height
import kotlinx.css.vh
import kotlinx.css.vw
import kotlinx.css.width
import react.child
import react.dom.render
import space.kscience.dataforge.context.Context
import space.kscience.gdml.GdmlShowCase
import space.kscience.visionforge.Application
import space.kscience.visionforge.VisionClient
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.ring.ThreeCanvasWithControls
import space.kscience.visionforge.ring.ThreeWithControlsPlugin
import space.kscience.visionforge.startApplication
import styled.css
import styled.styledDiv

private class JsPlaygroundApp : Application {

    override fun start(state: Map<String, Any>) {

        val playgroundContext = Context {
            plugin(ThreeWithControlsPlugin)
            plugin(VisionClient)
        }

        val element = document.getElementById("playground") ?: error("Element with id 'playground' not found on page")

        val visionOfD0 = GdmlShowCase.babyIaxo().toVision()

        render(element) {
            styledDiv {
                css{
                    height = 100.vh
                    width = 100.vw
                }
                child(ThreeCanvasWithControls) {
                    attrs {
                        context = playgroundContext
                        solid = visionOfD0
                    }
                }
            }
        }
    }
}

public fun main() {
    startApplication(::JsPlaygroundApp)
}