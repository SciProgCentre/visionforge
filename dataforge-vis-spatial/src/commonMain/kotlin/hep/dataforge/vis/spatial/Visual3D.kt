package hep.dataforge.vis.spatial

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.Context
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.io.serialization.ConfigSerializer
import hep.dataforge.io.serialization.MetaSerializer
import hep.dataforge.io.serialization.NameSerializer
import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vis.common.Visual
import hep.dataforge.vis.common.VisualObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlin.reflect.KClass

class Visual3D(meta: Meta) : AbstractPlugin(meta) {

    val visual by require(Visual)

    override val tag: PluginTag get() = Companion.tag

    override fun provideTop(target: String): Map<Name, Any> = if (target == Visual.VISUAL_FACTORY_TYPE) {
        mapOf(Box.TYPE_NAME.toName() to Box)
    } else {
        super.provideTop(target)
    }


    companion object : PluginFactory<Visual3D> {
        override val tag: PluginTag = PluginTag(name = "visual.spatial", group = PluginTag.DATAFORGE_GROUP)
        override val type: KClass<out Visual3D> = Visual3D::class
        override fun invoke(meta: Meta, context: Context): Visual3D = Visual3D(meta)

        val serialModule = SerializersModule {
            contextual(Point3DSerializer)
            contextual(Point2DSerializer)
            contextual(NameSerializer)
            contextual(NameTokenSerializer)
            contextual(MetaSerializer)
            contextual(ConfigSerializer)

            polymorphic(VisualObject::class, VisualObject3D::class) {
                VisualGroup3D::class with VisualGroup3D.serializer()
                Proxy::class with Proxy.serializer()
                Composite::class with Composite.serializer()
                Tube::class with Tube.serializer()
                Box::class with Box.serializer()
                Convex::class with Convex.serializer()
                Extruded::class with Extruded.serializer()
                addSubclass(PolyLine.serializer())
                addSubclass(Label3D.serializer())
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