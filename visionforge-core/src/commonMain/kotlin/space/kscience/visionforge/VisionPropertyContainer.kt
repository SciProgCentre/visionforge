package space.kscience.visionforge

import space.kscience.dataforge.meta.Config
import space.kscience.dataforge.meta.MetaItem
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.set
import space.kscience.dataforge.names.Name

/**
 * Property containers are used to create a symmetric behaviors for vision properties and style builders
 */
public interface VisionPropertyContainer<out V: Vision> {
    public fun getProperty(
        name: Name,
        inherit: Boolean = false,
        includeStyles: Boolean = true,
        includeDefaults: Boolean = true,
    ): MetaItem?

    public fun setProperty(name: Name, item: MetaItem?, notify: Boolean = true)
}

public open class SimpleVisionPropertyContainer<out V: Vision>(protected val config: Config): VisionPropertyContainer<V>{
    override fun getProperty(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean
    ): MetaItem? = config[name]

    override fun setProperty(name: Name, item: MetaItem?, notify: Boolean) {
        config[name] = item
    }

}