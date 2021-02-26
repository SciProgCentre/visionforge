package hep.dataforge.vision

import hep.dataforge.context.Context
import hep.dataforge.misc.DFExperimental

public actual object VisionForge

private val visionForgeContext = Context("VisionForge")

public actual val VisionForge.context: Context get() = visionForgeContext

@DFExperimental
public operator fun VisionForge.invoke(block: VisionForge.() -> Unit): Unit = run(block)