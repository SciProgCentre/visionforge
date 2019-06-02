package hep.dataforge.vis.spatial.gdml

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.Context
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.meta.Meta
import hep.dataforge.names.toName
import hep.dataforge.vis.spatial.ThreePlugin
import kotlin.reflect.KClass

class GDMLPlugin : AbstractPlugin() {
    override val tag: PluginTag get() = GDMLPlugin.tag

    override fun dependsOn() = listOf(ThreePlugin)

    override fun attach(context: Context) {
        super.attach(context)
        context.plugins.get<ThreePlugin>()?.factories?.apply {
            this["gdml".toName()] = ThreeGDMLFactory
        }
    }

    override fun detach() {
//        context.plugins.get<ThreePlugin>()?.factories?.apply {
//            remove("jsRoot.geometry".toName())
//            remove("jsRoot.object".toName())
//        }
        super.detach()
    }

    companion object : PluginFactory<GDMLPlugin> {
        override val tag = PluginTag("vis.gdml", "hep.dataforge")
        override val type: KClass<GDMLPlugin> = GDMLPlugin::class
        override fun invoke(meta: Meta) = GDMLPlugin()
    }
}