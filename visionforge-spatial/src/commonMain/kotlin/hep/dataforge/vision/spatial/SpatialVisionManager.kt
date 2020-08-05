package hep.dataforge.vision.spatial

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.Context
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vision.SimpleVisionGroup
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionManager
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlin.reflect.KClass

class SpatialVisionManager(meta: Meta) : AbstractPlugin(meta) {

    val visionManager by require(VisionManager)

    override val tag: PluginTag get() = Companion.tag

    override fun provideTop(target: String): Map<Name, Any> = if (target == VisionManager.VISION_FACTORY_TYPE) {
        mapOf(Box.TYPE_NAME.toName() to Box)
    } else {
        super.provideTop(target)
    }


    companion object : PluginFactory<SpatialVisionManager> {
        override val tag: PluginTag = PluginTag(name = "visual.spatial", group = PluginTag.DATAFORGE_GROUP)
        override val type: KClass<out SpatialVisionManager> = SpatialVisionManager::class
        override fun invoke(meta: Meta, context: Context): SpatialVisionManager = SpatialVisionManager(meta)

        val serialModule = SerializersModule {
            contextual(Point3DSerializer)
            contextual(Point2DSerializer)

            polymorphic(Vision::class, Vision3D::class) {
                subclass(SimpleVisionGroup.serializer())
                subclass(VisionGroup3D.serializer())
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

internal fun Vision3D.update(meta: Meta) {
    fun Meta.toVector(default: Float = 0f) = Point3D(
        this[Vision3D.X_KEY].float ?: default,
        this[Vision3D.Y_KEY].float ?: default,
        this[Vision3D.Z_KEY].float ?: default
    )

    meta[Vision3D.POSITION_KEY].node?.toVector()?.let { position = it }
    meta[Vision3D.ROTATION].node?.toVector()?.let { rotation = it }
    meta[Vision3D.SCALE_KEY].node?.toVector(1f)?.let { scale = it }
    meta["properties"].node?.let { configure(it) }
}