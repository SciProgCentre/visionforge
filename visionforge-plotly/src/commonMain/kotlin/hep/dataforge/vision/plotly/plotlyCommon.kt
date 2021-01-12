package hep.dataforge.vision.plotly

import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionPlugin
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

public expect class PlotlyPlugin : VisionPlugin

internal val plotlySerializersModule = SerializersModule {
    polymorphic(Vision::class) {
        subclass(VisionOfPlotly.serializer())
    }
}