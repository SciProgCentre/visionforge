package hep.dataforge.vis.spatial

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.names.set

class ThreePlugin : AbstractPlugin() {
    override val tag: PluginTag get() = ThreePlugin.tag

    val factories = HashMap<Name, ThreeFactory<*>>()

    init {
        factories["box"] = ThreeBoxFactory
        factories["convex"] = ThreeConvexFactory
    }

    override fun listNames(target: String): Sequence<Name> {
        return when (target) {
            ThreeFactory.TYPE -> factories.keys.asSequence()
            else -> return super.listNames(target)
        }
    }

    override fun provideTop(target: String, name: Name): Any? {
        return when (target) {
            ThreeFactory.TYPE -> factories[name]
            else -> return super.provideTop(target, name)
        }
    }

    companion object : PluginFactory<ThreePlugin> {
        override val tag = PluginTag("vis.three", "hep.dataforge")
        override val type = ThreePlugin::class
        override fun invoke(meta: Meta)  = ThreePlugin()
    }
}