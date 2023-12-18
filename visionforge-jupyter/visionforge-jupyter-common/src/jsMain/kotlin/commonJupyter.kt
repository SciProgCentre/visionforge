package space.kscience.visionforge.gdml.jupyter

import space.kscience.visionforge.jupyter.VFNotebookClient
import space.kscience.visionforge.markup.MarkupPlugin
import space.kscience.visionforge.plotly.PlotlyPlugin
import space.kscience.visionforge.runVisionClient
import space.kscience.visionforge.solid.three.ThreePlugin
import space.kscience.visionforge.tables.TableVisionJsPlugin

public fun main(): Unit = runVisionClient {
    plugin(ThreePlugin)
    plugin(PlotlyPlugin)
    plugin(MarkupPlugin)
    plugin(TableVisionJsPlugin)
    plugin(VFNotebookClient)
}

