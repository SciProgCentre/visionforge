package space.kscience.visionforge

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.toJson
import space.kscience.dataforge.meta.toMeta
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.html.*

public class VisionManager(meta: Meta) : AbstractPlugin(meta), MutableVisionContainer<Vision> {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Combined [SerializersModule] for all registered visions
     */
    public val serializersModule: SerializersModule by lazy {
        SerializersModule {
            include(defaultSerialModule)
            context.gather<SerializersModule>(VISION_SERIALIZER_MODULE_TARGET).values.forEach {
                include(it)
            }
        }
    }

    public val jsonFormat: Json by lazy {
        Json(defaultJson) {
            serializersModule = this@VisionManager.serializersModule
        }
    }

    public fun decodeFromString(string: String): Vision = jsonFormat.decodeFromString(visionSerializer, string)

    public fun encodeToString(vision: Vision): String = jsonFormat.encodeToString(visionSerializer, vision)
    public fun encodeToString(change: VisionChange): String =
        jsonFormat.encodeToString(VisionChange.serializer(), change)

    public fun decodeFromJson(json: JsonElement): Vision = jsonFormat.decodeFromJsonElement(visionSerializer, json)

    public fun encodeToJsonElement(vision: Vision): JsonElement =
        jsonFormat.encodeToJsonElement(visionSerializer, vision)

    //TODO remove double transformation with dedicated Meta serial format
    public fun decodeFromMeta(meta: Meta, descriptor: MetaDescriptor? = null): Vision =
        decodeFromJson(meta.toJson(descriptor))

    public fun encodeToMeta(vision: Vision, descriptor: MetaDescriptor? = null): Meta =
        encodeToJsonElement(vision).toMeta(descriptor)

    override fun setChild(name: Name?, child: Vision?) {
        child?.setAsRoot(this)
    }

    public companion object : PluginFactory<VisionManager> {
        override val tag: PluginTag = PluginTag(name = "vision", group = PluginTag.DATAFORGE_GROUP)

        public const val VISION_SERIALIZER_MODULE_TARGET: String = "visionSerializerModule"

        override fun build(context: Context, meta: Meta): VisionManager = VisionManager(meta)

        private val defaultSerialModule: SerializersModule = SerializersModule {
            polymorphic(Vision::class) {
                defaultDeserializer { SimpleVisionGroup.serializer() }
                subclass(NullVision.serializer())
                subclass(SimpleVisionGroup.serializer())
                subclass(VisionOfPlainHtml.serializer())
                subclass(VisionOfHtmlInput.serializer())
                subclass(VisionOfNumberField.serializer())
                subclass(VisionOfTextField.serializer())
                subclass(VisionOfCheckbox.serializer())
                subclass(VisionOfRangeField.serializer())
                subclass(VisionOfHtmlForm.serializer())
                subclass(VisionOfHtmlButton.serializer())
            }

            polymorphic(VisionEvent::class) {
                subclass(VisionChange.serializer())
                subclass(VisionMetaEvent.serializer())
                subclass(VisionSubmitEvent.serializer())
                subclass(VisionValueChangeEvent.serializer())
                subclass(VisionInputEvent.serializer())
            }
        }

        @OptIn(ExperimentalSerializationApi::class)
        public val defaultJson: Json = Json {
            serializersModule = defaultSerialModule
            prettyPrint = true
            useArrayPolymorphism = false
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        internal val visionSerializer: PolymorphicSerializer<Vision> = PolymorphicSerializer(Vision::class)
    }
}

public abstract class VisionPlugin(meta: Meta = Meta.EMPTY) : AbstractPlugin(meta) {
    public val visionManager: VisionManager by require(VisionManager)

    protected abstract val visionSerializersModule: SerializersModule

    override fun content(target: String): Map<Name, Any> = when (target) {
        VisionManager.VISION_SERIALIZER_MODULE_TARGET -> mapOf(Name.parse(tag.toString()) to visionSerializersModule)
        else -> super.content(target)
    }
}

/**
 * Fetch a [VisionManager] from this plugin or create a child plugin with a [VisionManager]
 */
public val Context.visionManager: VisionManager get() = request(VisionManager)

public fun Vision.encodeToString(): String =
    manager?.encodeToString(this) ?: error("Orphan vision could not be encoded")

/**
 * A root vision attached to [VisionManager]
 */
public class RootVision(override val manager: VisionManager) : AbstractVisionGroup() {
    override fun createGroup(): SimpleVisionGroup = SimpleVisionGroup()
}

/**
 * Designate this [Vision] as a root and assign a [VisionManager] as its parent
 */
public fun Vision.setAsRoot(manager: VisionManager) {
    if (parent != null) error("Vision $this already has a parent. It could not be set as root")
    parent = RootVision(manager)
}