package hep.dataforge.vision

import hep.dataforge.context.*
import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.toName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass


@DFExperimental
interface VisionForm<T : Vision> {
    val type: KClass<out T>
    val serializer: KSerializer<T>

    val name get() = serializer.descriptor.serialName.toName()

    /**
     * Apply a patch to given [Vision]
     */
    fun patch(obj: T, meta: Meta)

    companion object {
        const val TYPE = "visionForm"
    }
}

@DFExperimental
object SimpleGroupForm: VisionForm<SimpleVisionGroup>{
    override val type: KClass<out SimpleVisionGroup> = SimpleVisionGroup::class
    override val serializer: KSerializer<SimpleVisionGroup> = SimpleVisionGroup.serializer()

    override fun patch(obj: SimpleVisionGroup, meta: Meta) {
        TODO("Not yet implemented")
    }

}

@DFExperimental
fun <T : Vision> VisionForm<T>.visionToMeta(vision: T, module: SerialModule, descriptor: NodeDescriptor? = null): Meta {
    val engine = Json(VisionManager.jsonConfiguration, module)
    val json = engine.toJson<T>(serializer, vision)
    return json.toMetaItem(descriptor).node!!
}

@DFExperimental
fun <T : Vision> VisionForm<T>.buildVision(meta: Meta, module: SerialModule, descriptor: NodeDescriptor? = null): T {
    val engine = Json(VisionManager.jsonConfiguration, module)
    val json = meta.toJson(descriptor)
    return engine.fromJson(serializer, json)
}

@DFExperimental
class VisionManager(meta: Meta) : AbstractPlugin(meta) {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Create a list of factories on first call and cache it
     */
    private val forms by lazy {
        context.content<VisionForm<*>>(VisionForm.TYPE).mapKeys { it.value.type }
    }

    val visionSerialModule
        get() = SerializersModule {
            include(defaultSerialModule)
            context.content<SerialModule>(VISION_SERIAL_MODULE_TARGET).values.forEach {
                include(it)
            }
        }

    @Suppress("UNCHECKED_CAST")
    fun <T : Vision> resolveVisionForm(type: KClass<out T>): VisionForm<T>? =
        forms[type] as VisionForm<T>

    inline fun <reified T : Vision> buildSpecificVision(meta: Meta): T {
        val factory = resolveVisionForm(T::class) ?: error("Could not resolve a form for ${meta["type"].string}")
        return factory.buildVision(meta, visionSerialModule)
    }

    fun buildVision(meta: Meta): Vision {
        val type = meta["type"].string ?: SimpleVisionGroup.serializer().descriptor.serialName
        val form = forms.values.find { it.name.toString() == type } ?: error("Could not resolve a form for type $type")
        return form.buildVision(meta, visionSerialModule)
    }

    fun <T : Vision> writeVisionToMeta(vision: T): Meta {
        val form = resolveVisionForm(vision::class) ?: error("Could not resolve a form for $vision")
        val engine = Json(VisionManager.jsonConfiguration, visionSerialModule)
        val json = engine.toJson(form.serializer,vision)
        return json.toMetaItem().node!!
    }

    fun patchVision(vision: Vision, meta: Meta) {
        val form = resolveVisionForm(vision::class) ?: error("Could not resolve a form for $vision")
        form.patch(vision, meta)
    }

    companion object : PluginFactory<VisionManager> {
        override val tag: PluginTag = PluginTag(name = "vision", group = PluginTag.DATAFORGE_GROUP)
        override val type: KClass<out VisionManager> = VisionManager::class

        const val VISION_SERIAL_MODULE_TARGET = "visionSerialModule"

        override fun invoke(meta: Meta, context: Context): VisionManager = VisionManager(meta)

        @OptIn(UnstableDefault::class)
        val jsonConfiguration = JsonConfiguration(
            prettyPrint = true,
            useArrayPolymorphism = false,
            encodeDefaults = false,
            ignoreUnknownKeys = true
        )

        val defaultSerialModule = SerializersModule {
            polymorphic(Vision::class, VisionGroup::class) {
                subclass(SimpleVisionGroup.serializer())
            }
        }

    }
}