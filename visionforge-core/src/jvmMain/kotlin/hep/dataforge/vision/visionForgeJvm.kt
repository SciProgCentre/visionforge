package hep.dataforge.vision

import hep.dataforge.context.Context
import hep.dataforge.context.PluginFactory
import hep.dataforge.misc.DFExperimental

@DFExperimental
public actual object VisionForge

@DFExperimental
private val visionForgeContext = Context("VisionForge")

@DFExperimental
public actual val VisionForge.context: Context get() = visionForgeContext

@DFExperimental
public operator fun VisionForge.invoke(vararg modules: PluginFactory<out VisionPlugin>, block: VisionForge.() -> Unit): Unit {
    modules.forEach {
        plugins.fetch(it)
    }
    run(block)
}