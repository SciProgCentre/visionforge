import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.css.*
import react.Props
import react.fc
import space.kscience.dataforge.context.Context
import space.kscience.plotly.layout
import space.kscience.plotly.models.Trace
import space.kscience.visionforge.markup.VisionOfMarkup
import space.kscience.visionforge.react.flexRow
import space.kscience.visionforge.ring.ThreeCanvasWithControls
import space.kscience.visionforge.ring.solid
import space.kscience.visionforge.solid.*
import styled.css
import styled.styledDiv
import kotlin.math.sqrt

external interface DemoProps : Props {
    var context: Context
}

val GravityDemo = fc<DemoProps> { props ->
    val velocityTrace = Trace{
        name = "velocity"
    }
    val energyTrace = Trace{
        name = "energy"
    }
    val markup = VisionOfMarkup()

    styledDiv {
        css {
            height = 100.vh - 50.pt
        }
        styledDiv {
            css {
                height = 50.vh
            }
            child(ThreeCanvasWithControls) {
                attrs {
                    context = props.context
                    solid {
                        sphere(5.0, "ball") {
                            detail = 16
                            color("red")
                            val h = 100.0
                            y = h
                            context.launch {
                                val g = 10.0
                                val dt = 0.1
                                var time = 0.0
                                var velocity = 0.0
                                while (isActive) {
                                    delay(20)
                                    time += dt
                                    velocity -= g * dt
                                    val energy = g * y.toDouble() + velocity * velocity / 2
                                    y = y.toDouble() + velocity * dt

                                    velocityTrace.appendXYLatest(time, y)
                                    energyTrace.appendXYLatest(time, energy)

                                    if (y.toDouble() <= 2.5) {
                                        //conservation of energy
                                        velocity = sqrt(2 * g * h)
                                    }

                                    markup.content = """
                                        ## Bouncing sphere parameters
                                        
                                        **velocity** = $velocity
                                        
                                        **energy** = $energy
                                    """.trimIndent()
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
        flexRow {
            css {
                alignContent = Align.stretch
                alignItems = Align.stretch
                height = 50.vh - 50.pt
            }
            plotly {
                traces(velocityTrace,energyTrace)
                layout {
                    xaxis.title = "time"
                }
            }
            Markup {
                attrs {
                    this.markup = markup
                }
            }
        }
    }
}