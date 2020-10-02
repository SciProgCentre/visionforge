package hep.dataforge.vision

import hep.dataforge.context.*
import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.reflect.KClass


@DFExperimental
public interface VisionForm<T : Vision> {
    public val type: KClass<out T>
    public val serializer: KSerializer<T>

    public val name: Name
        get() = serializer.descriptor.serialName.toName()

    /**
     * Apply a patch to given [Vision]
     */
    public fun patch(obj: T, meta: Meta)

    public companion object {
        public const val TYPE: String = "visionForm"
    }
}

@DFExperimental
public object SimpleGroupForm : VisionForm<SimpleVisionGroup> {
    override val type: KClass<out SimpleVisionGroup> = SimpleVisionGroup::class
    override val serializer: KSerializer<SimpleVisionGroup> = SimpleVisionGroup.serializer()

    override fun patch(obj: SimpleVisionGroup, meta: Meta) {
        TODO("Not yet implemented")
    }

}

@DFExperimental
public fun <T : Vision> VisionForm<T>.visionToMeta(
    vision: T,
    module: SerializersModule,
    descriptor: NodeDescriptor? = null,
): Meta {
    val engine = Json(VisionManager.jsonConfiguration) { serializersModule = module }
    val json = engine.encodeToJsonElement(serializer, vision)
    return json.toMetaItem(descriptor).node!!
}

@DFExperimental
public fun <T : Vision> VisionForm<T>.buildVision(
    meta: Meta,
    module: SerializersModule,
    descriptor: NodeDescriptor? = null,
): T {
    val engine = Json(VisionManager.jsonConfiguration) { serializersModule = module }
    val json = meta.toJson(descriptor)
    return engine.decodeFromJsonElement(serializer, json)
}

@DFExperimental
public class VisionManager(meta: Meta) : AbstractPlugin(meta) {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Create a list of factories on first call and cache it
     */
    private val forms by lazy {
        context.gather<VisionForm<*>>(VisionForm.TYPE).mapKeys { it.value.type }
    }

    public val visionSerialModule: SerializersModule
        get() = SerializersModule {
            include(defaultSerialModule)
            context.gather<SerializersModule>(VISION_SERIAL_MODULE_TARGET).values.forEach {
                include(it)
            }
        }

    @Suppress("UNCHECKED_CAST")
    public fun <T : Vision> resolveVisionForm(type: KClass<out T>): VisionForm<T> =
        forms[type] as VisionForm<T>

    public inline fun <reified T : Vision> buildSpecificVision(meta: Meta): T {
        val factory = resolveVisionForm(T::class) ?: error("Could not resolve a form for ${meta["type"].string}")
        return factory.buildVision(meta, visionSerialModule)
    }

    @OptIn(ExperimentalSerializationApi::class)
    public fun buildVision(meta: Meta): Vision {
        val type = meta["type"].string ?: Vision.serializer().descriptor.serialName
        val form = forms.values.find { it.name.toString() == type } ?: error("Could not resolve a form for type $type")
        return form.buildVision(meta, visionSerialModule)
    }

    public fun <T : Vision> writeVisionToMeta(vision: T): Meta {
        val form = resolveVisionForm(vision::class) ?: error("Could not resolve a form for $vision")
        val engine = Json(jsonConfiguration) { serializersModule = visionSerialModule }
        val json = engine.encodeToJsonElement(form.serializer, vision)
        return json.toMetaItem().node!!
    }

    public fun patchVision(vision: Vision, meta: Meta) {
        val form = resolveVisionForm(vision::class) ?: error("Could not resolve a form for $vision")
        form.patch(vision, meta)
    }

    public companion object : PluginFactory<VisionManager> {
        override val tag: PluginTag = PluginTag(name = "vision", group = PluginTag.DATAFORGE_GROUP)
        override val type: KClass<out VisionManager> = VisionManager::class

        public const val VISION_SERIAL_MODULE_TARGET: String = "visionSerialModule"

        override fun invoke(meta: Meta, context: Context): VisionManager = VisionManager(meta)

        public val jsonConfiguration: Json = Json {
            prettyPrint = true
            useArrayPolymorphism = false
            encodeDefaults = false
            ignoreUnknownKeys = true
        }

        public val defaultSerialModule: SerializersModule = SerializersModule {
            polymorphic(Vision::class) {
                subclass(SimpleVisionGroup.serializer())
            }
        }
    }
}