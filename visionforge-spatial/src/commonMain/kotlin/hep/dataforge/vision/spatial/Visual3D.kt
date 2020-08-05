package hep.dataforge.vision.spatial

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.Context
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vision.SimpleVisualGroup
import hep.dataforge.vision.Visual
import hep.dataforge.vision.VisualObject
import kotlinx.serialization.UnstableDefault
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

            polymorphic(VisualObject::class, VisualObject3D::class) {
                subclass(SimpleVisualGroup.serializer())
                subclass(VisualGroup3D.serializer())
                subclass(Proxy.serializer())
                subclass(Composite.serializer())
                subclass(Tube.serializer())
                subclass(Box.serializer())
                subclass(Convex.serializer())
                subclass(Extruded.serializer())
                subclass(PolyLine.serializer())
                subclass(Label3D.serializer())
                subclass(Sphere.serializer())
            }
        }

        @OptIn(UnstableDefault::class)
        val json = Json(
            JsonConfiguration(
                prettyPrint = true,
                useArrayPolymorphism = false,
                encodeDefaults = false,
                ignoreUnknownKeys = true
            ),
            context = serialModule
        )
    }
}

internal fun VisualObject3D.update(meta: Meta) {
    fun Meta.toVector(default: Float = 0f) = Point3D(
        this[VisualObject3D.X_KEY].float ?: default,
        this[VisualObject3D.Y_KEY].float ?: default,
        this[VisualObject3D.Z_KEY].float ?: default
    )

    meta[VisualObject3D.POSITION_KEY].node?.toVector()?.let { position = it }
    meta[VisualObject3D.ROTATION].node?.toVector()?.let { rotation = it }
    meta[VisualObject3D.SCALE_KEY].node?.toVector(1f)?.let { scale = it }
    meta["properties"].node?.let { configure(it) }
}