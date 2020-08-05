package hep.dataforge.vision

import hep.dataforge.context.*
import hep.dataforge.meta.Meta
import kotlin.reflect.KClass

interface VisualFactory<T : Vision> {
    val type: KClass<T>
    operator fun invoke(
        context: Context,
        parent: Vision?,
        meta: Meta
    ): T
}

class VisionManager(meta: Meta) : AbstractPlugin(meta) {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Create a list of factories on first call and cache it
     */
    val visualFactories by lazy {
        context.content<VisualFactory<*>>(VISUAL_FACTORY_TYPE).mapKeys { it.value.type }
    }

    inline fun <reified T : Vision> buildVisual(parent: Vision?, meta: Meta): T? {
        return visualFactories[T::class]?.invoke(context, parent, meta) as T?
    }

    companion object : PluginFactory<VisionManager> {
        override val tag: PluginTag = PluginTag(name = "vision", group = PluginTag.DATAFORGE_GROUP)
        override val type: KClass<out VisionManager> = VisionManager::class

        override fun invoke(meta: Meta, context: Context): VisionManager = VisionManager(meta)

        const val VISUAL_FACTORY_TYPE = "vision.factory"
    }
}