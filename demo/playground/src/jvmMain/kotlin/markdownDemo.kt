package space.kscience.visionforge.examples

import kotlinx.html.div
import kotlinx.html.h1
import space.kscience.dataforge.context.Context
import space.kscience.plotly.layout
import space.kscience.plotly.models.ScatterMode
import space.kscience.plotly.models.TextPosition
import space.kscience.plotly.scatter
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.markup.markdown
import space.kscience.visionforge.plotly.PlotlyPlugin
import space.kscience.visionforge.plotly.plotly
import space.kscience.visionforge.solid.*

fun main() {
    val context = Context {
        plugin(Solids)
        plugin(PlotlyPlugin)
    }

    context.makeVisionFile(resourceLocation = ResourceLocation.SYSTEM) {
        markdown {
            //language=markdown
            """
                # Section
                
                **TBD**
                
                ## Subsection
            """.trimIndent()
        }

        div {
            h1 { +"Canvas" }
            vision("canvas") {
                solid {
                    box(100, 100, 100)
                    material {
                        emissiveColor("red")
                    }
                }
            }
        }

        vision("plot") {
            plotly {
                scatter {
                    x(1, 2, 3, 4)
                    y(10, 15, 13, 17)
                    mode = ScatterMode.markers
                    name = "Team A"
                    text("A-1", "A-2", "A-3", "A-4", "A-5")
                    textposition = TextPosition.`top center`
                    textfont {
                        family = "Raleway, sans-serif"
                    }
                    marker { size = 12 }
                }

                scatter {
                    x(2, 3, 4, 5)
                    y(10, 15, 13, 17)
                    mode = ScatterMode.lines
                    name = "Team B"
                    text("B-a", "B-b", "B-c", "B-d", "B-e")
                    textposition = TextPosition.`bottom center`
                    textfont {
                        family = "Times New Roman"
                    }
                    marker { size = 12 }
                }

                layout {
                    title = "Data Labels Hover"
                    xaxis {
                        range(0.75..5.25)
                    }
                    legend {
                        y = 0.5
                        font {
                            family = "Arial, sans-serif"
                            size = 20
                            color("grey")
                        }
                    }
                }
            }
        }
    }
}