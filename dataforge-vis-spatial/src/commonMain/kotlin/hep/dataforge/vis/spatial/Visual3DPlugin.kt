package hep.dataforge.vis.spatial

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.vis.common.VisualPlugin
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

class Visual3DPlugin(meta: Meta) : AbstractPlugin(meta) {
    override val tag: PluginTag get() = Companion.tag

    override fun provideTop(target: String): Map<Name, Any> {
        return if (target == VisualPlugin.VISUAL_FACTORY_TYPE) {
            mapOf()
        } else {
            emptyMap()
        }
    }

    companion object : PluginFactory<Visual3DPlugin> {
        override val tag: PluginTag = PluginTag(name = "visual.spatial", group = PluginTag.DATAFORGE_GROUP)
        override val type: KClass<out Visual3DPlugin> = Visual3DPlugin::class
        override fun invoke(meta: Meta): Visual3DPlugin = Visual3DPlugin(meta)

        val serialModule = SerializersModule {
            polymorphic(VisualObject3D::class) {
                VisualGroup3D::class with VisualGroup3D.serializer()
                Proxy::class with Proxy.serializer()
                Composite::class with Composite.serializer()
                Tube::class with Tube.serializer()
                Box::class with Box.serializer()
                Convex::class with Convex.serializer()
            }
        }

        val json = Json(
            JsonConfiguration(
                prettyPrint = true,
                useArrayPolymorphism = false,
                encodeDefaults = false
            ),
            context = serialModule
        )
    }
}

internal fun VisualObject3D.update(meta: Meta) {
    fun Meta.toVector(default: Float = 0f) = Point3D(
        this[VisualObject3D.x].float ?: default,
        this[VisualObject3D.y].float ?: default,
        this[VisualObject3D.z].float ?: default
    )

    meta[VisualObject3D.position].node?.toVector()?.let { position = it }
    meta[VisualObject3D.rotation].node?.toVector()?.let { rotation = it }
    meta[VisualObject3D.scale].node?.toVector(1f)?.let { scale = it }
    meta["properties"].node?.let { configure(it) }
}