package hep.dataforge.vision

import hep.dataforge.context.Context
import hep.dataforge.context.PluginManager
import hep.dataforge.misc.DFExperimental

public expect object VisionForge

@DFExperimental
public expect val VisionForge.context: Context

@DFExperimental
public val VisionForge.plugins: PluginManager get() = context.plugins

@DFExperimental
public val VisionForge.visionManager: VisionManager get() = plugins.fetch(VisionManager)