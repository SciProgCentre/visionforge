import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.css.*
import react.child
import react.dom.render
import ringui.SmartTabs
import ringui.Tab
import space.kscience.dataforge.context.Context
import space.kscience.plotly.models.Trace
import space.kscience.plotly.scatter
import space.kscience.visionforge.Application
import space.kscience.visionforge.VisionClient
import space.kscience.visionforge.plotly.PlotlyPlugin
import space.kscience.visionforge.ring.ThreeCanvasWithControls
import space.kscience.visionforge.ring.ThreeWithControlsPlugin
import space.kscience.visionforge.ring.solid
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.startApplication
import styled.css
import styled.styledDiv
import kotlin.math.sqrt
import kotlin.random.Random

fun Trace.appendXYLatest(x: Number, y: Number, history: Int = 400, xErr: Number? = null, yErr: Number? = null) {
    this.x.numbers = (this.x.numbers + x).takeLast(history)
    this.y.numbers = (this.y.numbers + y).takeLast(history)
    xErr?.let { error_x.array = (error_x.array + xErr).takeLast(history) }
    yErr?.let { error_y.array = (error_y.array + yErr).takeLast(history) }
}


private class JsPlaygroundApp : Application {

    override fun start(state: Map<String, Any>) {

        val playgroundContext = Context {
            plugin(ThreeWithControlsPlugin)
            plugin(VisionClient)
            plugin(PlotlyPlugin)
        }

        val element = document.getElementById("playground") ?: error("Element with id 'playground' not found on page")

        val bouncingSphereTrace = Trace()

        render(element) {
            styledDiv {
                css {
                    padding(0.pt)
                    margin(0.pt)
                    height = 100.vh
                    width = 100.vw
                }
                SmartTabs("gravity") {
                    Tab("gravity") {
                        styledDiv {
                            css {
                                height = 50.vh
                            }
                            child(ThreeCanvasWithControls) {
                                attrs {
                                    context = playgroundContext
                                    solid {
                                        sphere(5.0, "ball") {
                                            detail = 16
                                            color("red")
                                            val h = 100.0
                                            y = h
                                            launch {
                                                val g = 10.0
                                                val dt = 0.1
                                                var time = 0.0
                                                var velocity = 0.0
                                                while (isActive) {
                                                    delay(20)
                                                    time += dt
                                                    velocity -= g * dt
                                                    y = y.toDouble() + velocity * dt
                                                    bouncingSphereTrace.appendXYLatest(time, y)
                                                    if (y.toDouble() <= 2.5) {
                                                        //conservation of energy
                                                        velocity = sqrt(2 * g * h)
                                                    }
                                                }
                                            }
                                        }

                                        box(200, 5, 200, name = "floor") {
                                            y = -2.5
                                        }
                                    }
                                }
                            }
                        }
                        styledDiv {
                            css {
                                height = 40.vh
                            }

                            Plotly {
                                attrs {
                                    context = playgroundContext
                                    plot = space.kscience.plotly.Plotly.plot {
                                        traces(bouncingSphereTrace)
                                    }
                                }
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
                                height = 90.vh
                            }
                            child(ThreeCanvasWithControls) {
                                val random = Random(112233)
                                attrs {
                                    context = playgroundContext
                                    solid {
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
                                context = playgroundContext
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