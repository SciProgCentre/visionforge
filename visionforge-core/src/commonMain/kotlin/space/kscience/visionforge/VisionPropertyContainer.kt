package space.kscience.visionforge

import space.kscience.dataforge.meta.Configurable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.ObservableMutableMeta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.values.Value

/**
 * Property containers are used to create a symmetric behaviors for vision properties and style builders
 */
public interface VisionPropertyContainer<out V : Vision> {
    public fun getProperty(
        name: Name,
        inherit: Boolean = false,
        includeStyles: Boolean = true,
        includeDefaults: Boolean = true,
    ): Meta?

    /**
     * Replace the property node. If [node] is null remove node and its descendants
     */
    public fun setPropertyNode(name: Name, node: Meta?, notify: Boolean = true)

    /**
     * Set a value of specific property node
     */
    public fun setPropertyValue(name: Name, value: Value?, notify: Boolean = true)
}

public open class SimpleVisionPropertyContainer<out V : Vision>(
    override val meta: ObservableMutableMeta,
) : VisionPropertyContainer<V>, Configurable {
    override fun getProperty(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean
    ): Meta? = meta[name]

    override fun setPropertyNode(name: Name, node: Meta?, notify: Boolean) {
        this.meta.setMeta(name, node)
    }

    override fun setPropertyValue(name: Name, value: Value?, notify: Boolean) {
        this.meta.setValue(name, value)
    }
}