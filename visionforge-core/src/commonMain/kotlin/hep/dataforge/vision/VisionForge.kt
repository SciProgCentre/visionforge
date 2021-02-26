package hep.dataforge.vision

import hep.dataforge.context.Context
import hep.dataforge.context.PluginManager

public expect object VisionForge

public expect val VisionForge.context: Context

public val VisionForge.plugins: PluginManager get() = context.plugins

public val VisionForge.visionManager: VisionManager get() = plugins.fetch(VisionManager)