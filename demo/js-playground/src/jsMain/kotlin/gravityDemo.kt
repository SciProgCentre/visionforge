import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import app.softwork.bootstrapcompose.Column
import app.softwork.bootstrapcompose.Row
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta
import space.kscience.plotly.Plot
import space.kscience.plotly.layout
import space.kscience.plotly.models.Trace
import space.kscience.visionforge.Colors
import space.kscience.visionforge.compose.Vision
import space.kscience.visionforge.compose.zIndex
import space.kscience.visionforge.markup.VisionOfMarkup
import space.kscience.visionforge.plotly.asVision
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.solid.three.compose.ThreeView
import kotlin.math.sqrt

@Composable
fun Plot(
    context: Context,
    meta: Meta = Meta.EMPTY,
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
    block: Plot.() -> Unit,
) = Vision(
    context = context,
    attrs = attrs,
    meta = meta,
    vision = Plot().apply(block).asVision()
)

@Composable
fun Markup(
    context: Context,
    markup: VisionOfMarkup,
    meta: Meta = Meta.EMPTY,
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
) = Vision(
    context = context,
    attrs = attrs,
    meta = meta,
    vision = markup
)


private val h = 100.0

@Composable
fun GravityDemo(context: Context) {
    val velocityTrace = remember {
        Trace {
            name = "velocity"
        }
    }

    val energyTrace = remember {
        Trace {
            name = "energy"
        }
    }

    val markup = remember { VisionOfMarkup() }

    val solid = remember {
        SolidGroup {
            pointLight(200, 200, 200, name = "light") {
                color(Colors.white)
            }
            ambientLight()

            sphere(5.0, "ball") {
                detail = 16
                color("red")
                y = h


                box(200, 5, 200, name = "floor") {
                    y = -2.5
                }
            }
        }
    }

    LaunchedEffect(solid) {
        val ball = solid["ball"]!!
        val g = 10.0
        val dt = 0.1
        var time = 0.0
        var velocity = 0.0
        while (isActive) {
            delay(20)
            time += dt
            velocity -= g * dt
            val energy = g * ball.y.toDouble() + velocity * velocity / 2
            ball.y = ball.y.toDouble() + velocity * dt

            velocityTrace.appendXYLatest(time, ball.y)
            energyTrace.appendXYLatest(time, energy)

            if (ball.y.toDouble() <= 2.5) {
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

    Div({
        style {
            height(100.vh - 50.pt)
        }
    }) {
        Div({
            style {
                height(50.vh)
            }
        }) {
            ThreeView(context, solid)
        }
        Row(attrs = {
            style {
                alignContent(AlignContent.Stretch)
                alignItems(AlignItems.Stretch)
                height(50.vh - 50.pt)
            }
        }) {
            Column {
                Plot(context) {
                    traces(velocityTrace, energyTrace)
                    layout {
                        xaxis.title = "time"
                    }
                }
            }
            Column {
                Markup(context, markup, attrs = {
                    style {
                        width(100.percent)
                        height(100.percent)
                        border(2.pt, LineStyle.Solid, Color.blue)
                        paddingLeft(8.pt)
                        backgroundColor(Color.white)
                        flex(1)
                        zIndex(10000)
                    }
                })
            }
        }
    }
}