package hep.dataforge.vis.spatial.jsroot

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.Context
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.names.toName
import hep.dataforge.vis.spatial.ThreePlugin

class JSRootPlugin : AbstractPlugin() {
    override val tag: PluginTag get() = JSRootPlugin.tag

    override fun dependsOn() = listOf(ThreePlugin)

    override fun attach(context: Context) {
        super.attach(context)
        context.plugins.get<ThreePlugin>()?.factories?.apply {
            this["jsRoot.geometry".toName()] = ThreeJSRootGeometryFactory
            this["jsRoot.object".toName()] = ThreeJSRootObjectFactory
        }
    }

    override fun detach() {
        context.plugins.get<ThreePlugin>()?.factories?.apply {
            remove("jsRoot.geometry".toName())
            remove("jsRoot.object".toName())
        }
        super.detach()
    }

    companion object: PluginFactory<JSRootPlugin> {
        override val tag = PluginTag("vis.jsroot", "hep.dataforge")
        override val type = JSRootPlugin::class
        override fun invoke() = JSRootPlugin()
    }
}