package hep.dataforge.vision.plotly

import hep.dataforge.context.PluginFactory
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.VisionPlugin
import hep.dataforge.vision.plugins
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

public expect class PlotlyPlugin : VisionPlugin{
    public companion object: PluginFactory<PlotlyPlugin>
}

internal val plotlySerializersModule = SerializersModule {
    polymorphic(Vision::class) {
        subclass(VisionOfPlotly.serializer())
    }
}

/**
 * Ensure that [PlotlyPlugin] is loaded in the global [VisionForge] context
 */
public fun VisionForge.usePlotly() {
    plugins.fetch(PlotlyPlugin)
}