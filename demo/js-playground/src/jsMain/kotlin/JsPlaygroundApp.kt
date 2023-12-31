import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.Document
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.request
import space.kscience.plotly.models.Trace
import space.kscience.plotly.scatter
import space.kscience.visionforge.Application
import space.kscience.visionforge.Colors
import space.kscience.visionforge.JsVisionClient
import space.kscience.visionforge.compose.Tabs
import space.kscience.visionforge.plotly.PlotlyPlugin
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.solid.three.ThreePlugin
import space.kscience.visionforge.solid.three.compose.ThreeView
import space.kscience.visionforge.startApplication
import kotlin.random.Random

fun Trace.appendXYLatest(x: Number, y: Number, history: Int = 400, xErr: Number? = null, yErr: Number? = null) {
    this.x.numbers = (this.x.numbers + x).takeLast(history)
    this.y.numbers = (this.y.numbers + y).takeLast(history)
    xErr?.let { error_x.array = (error_x.array + xErr).takeLast(history) }
    yErr?.let { error_y.array = (error_y.array + yErr).takeLast(history) }
}

private class JsPlaygroundApp : Application {

    override fun start(document: Document, state: Map<String, Any>) {

        val playgroundContext = Context {
            plugin(ThreePlugin)
            plugin(PlotlyPlugin)
        }

        val solids = playgroundContext.request(Solids)
        val client = playgroundContext.request(JsVisionClient)

        val element = document.getElementById("playground") ?: error("Element with id 'playground' not found on page")

        renderComposable(element) {
            Div({
                style {
                    padding(0.pt)
                    margin(0.pt)
                    height(100.vh)
                    width(100.vw)
                }
            }) {
                Tabs("gravity") {
                    Tab("gravity") {
                        GravityDemo(solids, client)
                    }

//                    Tab("D0") {
//                        child(ThreeCanvasWithControls) {
//                            attrs {
//                                context = playgroundContext
//                                solid = GdmlShowCase.babyIaxo().toVision()
//                            }
//                        }
//                    }
                    Tab("spheres") {
                        Div({
                            style {
                                height(100.vh - 50.pt)
                            }
                        }) {
                            ThreeView(solids, SolidGroup {
                                ambientLight {
                                    color(Colors.white)
                                }
                                repeat(100) {
                                    sphere(5, name = "sphere[$it]") {
                                        x = Random.nextDouble(-300.0, 300.0)
                                        y = Random.nextDouble(-300.0, 300.0)
                                        z = Random.nextDouble(-300.0, 300.0)
                                        material {
                                            color(Random.nextInt())
                                        }
                                        detail = 16
                                    }
                                }
                            })
                        }
                    }
                    Tab("plotly") {
                        Plot(client) {
                            scatter {
                                x(1, 2, 3)
                                y(5, 8, 7)
                            }
                        }
                    }
                }
            }
        }
    }

}

public fun main() {
    startApplication(::JsPlaygroundApp)
}