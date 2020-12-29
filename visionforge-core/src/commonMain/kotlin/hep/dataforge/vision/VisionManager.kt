package hep.dataforge.vision

import hep.dataforge.context.*
import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.reflect.KClass

public class VisionManager(meta: Meta) : AbstractPlugin(meta) {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Combined [SerializersModule] for all registered visions
     */
    public val serializersModule: SerializersModule
        get() = SerializersModule {
            include(defaultSerialModule)
            context.gather<SerializersModule>(VISION_SERIALIZER_MODULE_TARGET).values.forEach {
                include(it)
            }
        }

    public val jsonFormat: Json
        get() = Json(defaultJson) {
            serializersModule = this@VisionManager.serializersModule
        }

    public fun decodeFromString(string: String): Vision = jsonFormat.decodeFromString(visionSerializer, string)

    public fun encodeToString(vision: Vision): String = jsonFormat.encodeToString(visionSerializer, vision)

    public fun decodeFromJson(json: JsonElement): Vision = jsonFormat.decodeFromJsonElement(visionSerializer, json)

    public fun encodeToJsonElement(vision: Vision): JsonElement =
        jsonFormat.encodeToJsonElement(visionSerializer, vision)

    //TODO remove double transformation with dedicated Meta serial format
    public fun decodeFromMeta(meta: Meta, descriptor: NodeDescriptor? = null): Vision =
        decodeFromJson(meta.toJson(descriptor))

    public fun encodeToMeta(vision: Vision, descriptor: NodeDescriptor? = null): Meta =
        encodeToJsonElement(vision).toMetaItem(descriptor).node
            ?: error("Expected node, but value found. Check your serializer!")

    public companion object : PluginFactory<VisionManager> {
        override val tag: PluginTag = PluginTag(name = "vision", group = PluginTag.DATAFORGE_GROUP)
        override val type: KClass<out VisionManager> = VisionManager::class

        public const val VISION_SERIALIZER_MODULE_TARGET: String = "visionSerializerModule"

        override fun invoke(meta: Meta, context: Context): VisionManager = VisionManager(meta)

        private val defaultSerialModule: SerializersModule = SerializersModule {
            polymorphic(Vision::class) {
                default { VisionBase.serializer() }
                subclass(VisionBase.serializer())
                subclass(VisionGroupBase.serializer())
            }
        }

        public val defaultJson: Json = Json {
            serializersModule = defaultSerialModule
            prettyPrint = true
            useArrayPolymorphism = false
            encodeDefaults = false
            ignoreUnknownKeys = true
        }

        internal val visionSerializer: PolymorphicSerializer<Vision> = PolymorphicSerializer(Vision::class)
    }
}

/**
 * Fetch a [VisionManager] from this plugin
 */
public val Context.visionManager: VisionManager get() = plugins.fetch(VisionManager)