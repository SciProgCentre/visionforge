package space.kscience.visionforge

import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginManager
import space.kscience.dataforge.misc.DFExperimental

public expect object VisionForge

@DFExperimental
public expect val VisionForge.context: Context

@DFExperimental
public val VisionForge.plugins: PluginManager get() = context.plugins

@DFExperimental
public val VisionForge.visionManager: VisionManager get() = plugins.fetch(VisionManager)