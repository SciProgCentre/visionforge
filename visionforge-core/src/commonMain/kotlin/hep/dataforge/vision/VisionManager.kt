package hep.dataforge.vision

import hep.dataforge.context.*
import hep.dataforge.meta.Meta
import kotlin.reflect.KClass

interface VisionFactory<T : Vision> : Factory<T> {
    val type: KClass<T>
}

class VisionManager(meta: Meta) : AbstractPlugin(meta) {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Create a list of factories on first call and cache it
     */
    private val factories by lazy {
        context.content<VisionFactory<*>>(VISION_FACTORY_TYPE).mapKeys { it.value.type }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Vision> resolveVisionFactory(type: KClass<out T>): VisionFactory<T>? =
        factories[type] as VisionFactory<T>

    inline fun <reified T : Vision> buildVision(parent: VisionGroup?, meta: Meta): T? {
        return resolveVisionFactory(T::class)?.invoke(meta, context)?.apply {
            this.parent = parent
        }
    }

    companion object : PluginFactory<VisionManager> {
        override val tag: PluginTag = PluginTag(name = "vision", group = PluginTag.DATAFORGE_GROUP)
        override val type: KClass<out VisionManager> = VisionManager::class

        override fun invoke(meta: Meta, context: Context): VisionManager = VisionManager(meta)

        const val VISION_FACTORY_TYPE = "vision.factory"
    }
}