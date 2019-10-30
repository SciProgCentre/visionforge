package hep.dataforge.vis.common

import hep.dataforge.context.*
import hep.dataforge.meta.Meta
import kotlin.reflect.KClass

interface VisualFactory<T : VisualObject> {
    val type: KClass<T>
    operator fun invoke(
        context: Context,
        parent: VisualObject?,
        meta: Meta
    ): T
}

class VisualPlugin(meta: Meta) : AbstractPlugin(meta) {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Create a list of factories on first call and cache it
     */
    val visualFactories by lazy {
        context.content<VisualFactory<*>>(VISUAL_FACTORY_TYPE).mapKeys { it.value.type }
    }

    inline fun <reified T : VisualObject> buildVisual(parent: VisualObject?, meta: Meta): T? {
        return visualFactories[T::class]?.invoke(context, parent, meta) as T?
    }

    companion object : PluginFactory<VisualPlugin> {
        override val tag: PluginTag = PluginTag(name = "visual", group = PluginTag.DATAFORGE_GROUP)
        override val type: KClass<out VisualPlugin> = VisualPlugin::class

        override fun invoke(meta: Meta, context: Context): VisualPlugin = VisualPlugin(meta)

        const val VISUAL_FACTORY_TYPE = "visual.factory"
    }
}