package hep.dataforge.vis.jsroot

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vis.spatial.three.ThreeFactory
import hep.dataforge.vis.spatial.three.ThreePlugin

class JSRootPlugin : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag

    override fun dependsOn() = listOf(ThreePlugin)

    override fun provideTop(target: String): Map<Name, Any> {
        return when(target){
            ThreeFactory.TYPE -> mapOf(
                "jsRoot.geometry".toName() to ThreeJSRootGeometryFactory,
                "jsRoot.object".toName() to ThreeJSRootObjectFactory
            )
            else -> emptyMap()
        }
    }

    companion object: PluginFactory<JSRootPlugin> {
        override val tag = PluginTag("vis.jsroot", "hep.dataforge")
        override val type = JSRootPlugin::class
        override fun invoke(meta: Meta) = JSRootPlugin()
    }
}