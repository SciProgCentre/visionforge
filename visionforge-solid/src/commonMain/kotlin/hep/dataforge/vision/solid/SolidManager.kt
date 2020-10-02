package hep.dataforge.vision.solid

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.Context
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vision.SimpleVisionGroup
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionForm
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.VisionManager.Companion.VISION_SERIAL_MODULE_TARGET
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*
import kotlin.reflect.KClass

@DFExperimental
private class SolidForm<T : Solid>(
    override val type: KClass<T>,
    override val serializer: KSerializer<T>,
) : VisionForm<T> {

    private fun Solid.update(meta: Meta) {
        fun Meta.toVector(default: Float = 0f) = Point3D(
            this[Solid.X_KEY].float ?: default,
            this[Solid.Y_KEY].float ?: default,
            this[Solid.Z_KEY].float ?: default
        )

        meta[Solid.POSITION_KEY].node?.toVector()?.let { position = it }
        meta[Solid.ROTATION].node?.toVector()?.let { rotation = it }
        meta[Solid.SCALE_KEY].node?.toVector(1f)?.let { scale = it }
        meta["properties"].node?.let { configure(it) }
    }

    override fun patch(obj: T, meta: Meta) {
        TODO("Not yet implemented")
    }
}

@OptIn(ExperimentalSerializationApi::class)
@DFExperimental
private fun SerializersModule.extractFactories(): List<SolidForm<*>> {
    val list = ArrayList<SolidForm<*>>()

    val collector = object : SerializersModuleCollector {
        override fun <T : Any> contextual(kClass: KClass<T>, serializer: KSerializer<T>) {
            //Do nothing
        }

        override fun <Base : Any, Sub : Base> polymorphic(
            baseClass: KClass<Base>,
            actualClass: KClass<Sub>,
            actualSerializer: KSerializer<Sub>,
        ) {
            if (baseClass == Vision::class) {
                @Suppress("UNCHECKED_CAST") val factory = SolidForm<Solid>(
                    actualClass as KClass<Solid>,
                    actualSerializer as KSerializer<Solid>
                )
                list.add(factory)
            }
        }

        override fun <Base : Any> polymorphicDefault(
            baseClass: KClass<Base>,
            defaultSerializerProvider: (className: String?) -> DeserializationStrategy<out Base>?,
        ) {
            TODO("Not yet implemented")
        }

    }
    dumpTo(collector)
    return list
}

@DFExperimental
public class SolidManager(meta: Meta) : AbstractPlugin(meta) {

    public val visionManager: VisionManager by require(VisionManager)

    override val tag: PluginTag get() = Companion.tag

    override fun content(target: String): Map<Name, Any> = when (target) {
        VisionForm.TYPE -> serialModule.extractFactories().associateBy { it.name }
        VISION_SERIAL_MODULE_TARGET -> mapOf(tag.name.toName() to serialModule)
        else -> super.content(target)
    }

    public companion object : PluginFactory<SolidManager> {
        override val tag: PluginTag = PluginTag(name = "visual.spatial", group = PluginTag.DATAFORGE_GROUP)
        override val type: KClass<out SolidManager> = SolidManager::class
        override fun invoke(meta: Meta, context: Context): SolidManager = SolidManager(meta)

        public val serialModule: SerializersModule = SerializersModule {
            contextual(Point3DSerializer)
            contextual(Point2DSerializer)

            polymorphic(Vision::class) {
                subclass(SimpleVisionGroup.serializer())
                subclass(SolidGroup.serializer())
                subclass(Proxy.serializer())
                subclass(Composite.serializer())
                subclass(Tube.serializer())
                subclass(Box.serializer())
                subclass(Convex.serializer())
                subclass(Extruded.serializer())
                subclass(PolyLine.serializer())
                subclass(SolidLabel.serializer())
                subclass(Sphere.serializer())
            }
        }

        val jsonForSolids = Json {
            prettyPrint = true
            useArrayPolymorphism = false
            encodeDefaults = false
            ignoreUnknownKeys = true
            serializersModule = this@Companion.serialModule
        }
    }
}
