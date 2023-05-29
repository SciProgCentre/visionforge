package space.kscience.visionforge.markup

import space.kscience.dataforge.context.PluginFactory
import space.kscience.visionforge.VisionPlugin

public expect class MarkupPlugin: VisionPlugin{
    public companion object : PluginFactory<MarkupPlugin>
}