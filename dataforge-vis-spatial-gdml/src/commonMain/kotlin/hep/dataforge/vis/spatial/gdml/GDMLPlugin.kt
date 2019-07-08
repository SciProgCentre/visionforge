//package hep.dataforge.vis.spatial.gdml
//
//import hep.dataforge.context.AbstractPlugin
//import hep.dataforge.context.Context
//import hep.dataforge.context.PluginFactory
//import hep.dataforge.context.PluginTag
//import hep.dataforge.meta.Meta
//import hep.dataforge.names.Name
//import hep.dataforge.names.toName
//import hep.dataforge.vis.spatial.three.ThreeFactory
//import hep.dataforge.vis.spatial.three.ThreePlugin
//import kotlin.reflect.KClass
//
//class GDMLPlugin : AbstractPlugin() {
//    override val tag: PluginTag get() = GDMLPlugin.tag
//
//    override fun dependsOn() = listOf(ThreePlugin)
//
//    override fun provideTop(target: String): Map<Name, Any> {
//        return when(target){
//            ThreeFactory.TYPE-> mapOf("gdml".toName() to ThreeGDMLFactory)
//            else -> emptyMap()
//        }
//    }
//
//    companion object : PluginFactory<GDMLPlugin> {
//        override val tag = PluginTag("vis.gdml", "hep.dataforge")
//        override val type: KClass<GDMLPlugin> = GDMLPlugin::class
//        override fun invoke(meta: Meta) = GDMLPlugin()
//    }
//}