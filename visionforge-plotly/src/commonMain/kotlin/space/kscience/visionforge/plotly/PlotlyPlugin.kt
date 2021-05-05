package space.kscience.visionforge.plotly

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.dataforge.context.PluginFactory
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionPlugin

public expect class PlotlyPlugin : VisionPlugin {
    public companion object : PluginFactory<PlotlyPlugin>
}

internal val plotlySerializersModule = SerializersModule {
    polymorphic(Vision::class) {
        subclass(VisionOfPlotly.serializer())
    }
}