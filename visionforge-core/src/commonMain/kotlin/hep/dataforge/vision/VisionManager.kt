package hep.dataforge.vision

import hep.dataforge.context.*
import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
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
            context.gather<SerializersModule>(VISION_SERIAL_MODULE_TARGET).values.forEach {
                include(it)
            }
        }

    public val jsonFormat: Json
        get() = Json {
            prettyPrint = true
            useArrayPolymorphism = false
            encodeDefaults = false
            ignoreUnknownKeys = true
            serializersModule = this@VisionManager.serializersModule
        }

    public fun decodeFromString(string: String): Vision = jsonFormat.decodeFromString(Vision.serializer(), string)
    public fun encodeToString(vision: Vision): String = jsonFormat.encodeToString(Vision.serializer(), vision)

    public fun decodeFromJson(json: JsonElement): Vision = jsonFormat.decodeFromJsonElement(Vision.serializer(), json)
    public fun encodeToJsonElement(vision: Vision): JsonElement =
        jsonFormat.encodeToJsonElement(Vision.serializer(), vision)

    //TODO remove double transformation with dedicated Meta serial format
    public fun decodeFromMeta(meta: Meta, descriptor: NodeDescriptor? = null): Vision =
        decodeFromJson(meta.toJson(descriptor))

    public fun encodeToMeta(vision: Vision, descriptor: NodeDescriptor? = null): Meta =
        encodeToJsonElement(vision).toMetaItem(descriptor).node
            ?: error("Expected node, but value found. Check your serializer!")

//    public fun updateVision(vision: Vision, meta: Meta) {
//        vision.update(meta)
//        if (vision is MutableVisionGroup) {
//            val children by meta.node()
//            children?.items?.forEach { (token, item) ->
//                when {
//                    item.value == Null -> vision[token] = null //Null means removal
//                    item.node != null -> {
//                        val node = item.node!!
//                        val type by node.string()
//                        if (type != null) {
//                            //If the type is present considering it as new node, not an update
//                            vision[token.asName()] = decodeFromMeta(node)
//                        } else {
//                            val existing = vision.children[token]
//                            if (existing != null) {
//                                updateVision(existing, node)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    public companion object : PluginFactory<VisionManager> {
        override val tag: PluginTag = PluginTag(name = "vision", group = PluginTag.DATAFORGE_GROUP)
        override val type: KClass<out VisionManager> = VisionManager::class

        public const val VISION_SERIAL_MODULE_TARGET: String = "visionSerialModule"

        override fun invoke(meta: Meta, context: Context): VisionManager = VisionManager(meta)

        private val defaultSerialModule: SerializersModule = SerializersModule {
            polymorphic(Vision::class) {
                subclass(VisionGroupBase.serializer())
            }
        }
    }
}