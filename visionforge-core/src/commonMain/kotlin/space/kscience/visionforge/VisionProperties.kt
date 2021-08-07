package space.kscience.visionforge

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.values.Value

internal class VisionProperties(
    val vision: Vision,
    val rootName: Name,
    val inherit: Boolean = false,
    val includeStyles: Boolean = true,
    val includeDefaults: Boolean = true,
) : MutableMeta {

    override val items: Map<NameToken, MutableMeta>
        get() = vision.getProperty(rootName, inherit, includeStyles, includeDefaults)?.items?.mapValues {
            VisionProperties(vision, rootName + it.key, inherit, includeStyles, includeDefaults)
        } ?: emptyMap()

    override var value: Value?
        get() = vision.getProperty(rootName, inherit, includeStyles, includeDefaults)?.value
        set(value) {
            vision.setPropertyValue(rootName, value)
        }

    override fun getOrCreate(name: Name): MutableMeta = VisionProperties(vision, rootName + name)

    override fun setMeta(name: Name, node: Meta?) {
        vision.setPropertyNode(rootName + name, node)
    }

    override fun equals(other: Any?): Boolean = Meta.equals(this, other as? Meta)
    override fun hashCode(): Int = Meta.hashCode(this)
    override fun toString(): String = Meta.toString(this)
}