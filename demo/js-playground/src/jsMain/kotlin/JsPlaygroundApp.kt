import kotlinx.css.*
import org.w3c.dom.Document
import ringui.SmartTabs
import ringui.Tab
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.request
import space.kscience.plotly.models.Trace
import space.kscience.plotly.scatter
import space.kscience.visionforge.Application
import space.kscience.visionforge.Colors
import space.kscience.visionforge.JsVisionClient
import space.kscience.visionforge.plotly.PlotlyPlugin
import space.kscience.visionforge.react.createRoot
import space.kscience.visionforge.react.render
import space.kscience.visionforge.ring.ThreeCanvasWithControls
import space.kscience.visionforge.ring.ThreeWithControlsPlugin
import space.kscience.visionforge.ring.solid
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.startApplication
import styled.css
import styled.styledDiv
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
            plugin(ThreeWithControlsPlugin)
            plugin(JsVisionClient)
            plugin(PlotlyPlugin)
        }

        val element = document.getElementById("playground") ?: error("Element with id 'playground' not found on page")

        createRoot(element).render {
            styledDiv {
                css {
                    padding = Padding(0.pt)
                    margin = Margin(0.pt)
                    height = 100.vh
                    width = 100.vw
                }
                SmartTabs("gravity") {
                    Tab("gravity") {
                        GravityDemo {
                            attrs {
                                this.solids = playgroundContext.request(Solids)
                            }
                        }
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
                        styledDiv {
                            css {
                                height = 100.vh - 50.pt
                            }
                            child(ThreeCanvasWithControls) {
                                val random = Random(112233)
                                attrs {
                                    solids = playgroundContext.request(Solids)
                                    solid {
                                        ambientLight {
                                            color(Colors.white)
                                        }
                                        repeat(100) {
                                            sphere(5, name = "sphere[$it]") {
                                                x = random.nextDouble(-300.0, 300.0)
                                                y = random.nextDouble(-300.0, 300.0)
                                                z = random.nextDouble(-300.0, 300.0)
                                                material {
                                                    color(random.nextInt())
                                                }
                                                detail = 16
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Tab("plotly") {
                        Plotly {
                            attrs {
                                plot = space.kscience.plotly.Plotly.plot {
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
    }
}

public fun main() {
    startApplication(::JsPlaygroundApp)
}