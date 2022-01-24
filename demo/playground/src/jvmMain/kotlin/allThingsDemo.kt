package space.kscience.visionforge.examples

import kotlinx.html.h2
import space.kscience.dataforge.values.ValueType
import space.kscience.plotly.layout
import space.kscience.plotly.models.ScatterMode
import space.kscience.plotly.models.TextPosition
import space.kscience.plotly.scatter
import space.kscience.tables.ColumnHeader
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.markup.markdown
import space.kscience.visionforge.plotly.plotly
import space.kscience.visionforge.solid.box
import space.kscience.visionforge.solid.solid
import space.kscience.visionforge.solid.z
import space.kscience.visionforge.tables.columnTable
import java.nio.file.Paths


fun main() = makeVisionFile(
    Paths.get("VisionForgeDemo.html"),
    resourceLocation = ResourceLocation.EMBED
) {
    markdown {
        //language=markdown
        """
            # VisionForge
            
            This is a demo for current VisionForge features. This text is written in [MarkDown](https://github.com/JetBrains/markdown)
        """.trimIndent()
    }

    h2 { +"3D visualization with Three-js" }
    vision("3D") {
        solid {
            box(100, 100, 100, name = "aBox"){
                z = 50.0
            }
        }
    }

    h2 { +"Interactive plots with Plotly" }
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
    h2 { +"Interactive tables with Tabulator" }
    vision("table") {
        val x by ColumnHeader.value(ValueType.NUMBER)
        val y by ColumnHeader.value(ValueType.NUMBER)
        columnTable(
            x to listOf(2, 3, 4, 5),
            y to listOf(10, 15, 13, 17)
        )
    }
    markdown {
        //language=markdown
        """
            ## The code for everything above
            ```kotlin
            markdown {
                //language=markdown
                ""${'"'}
                    # VisionForge
                    
                    This is a demo for current VisionForge features. This text is written in [MarkDown](https://github.com/JetBrains/markdown)
                ""${'"'}.trimIndent()
            }
    
            h2 { +"3D visualization with Three-js" }
            vision("3D") {
                solid {
                    box(100, 100, 100, name = "aBox")
                }
            }
    
            h2 { +"Interactive plots with Plotly" }
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
            h2 { +"Interactive tables with Tabulator" }
            vision("table") {
                val x by ColumnHeader.value(ValueType.NUMBER)
                val y by ColumnHeader.value(ValueType.NUMBER)
                columnTable(
                    x to listOf(2, 3, 4, 5),
                    y to listOf(10, 15, 13, 17)
                )
            }
            ```
        """.trimIndent()
    }
}