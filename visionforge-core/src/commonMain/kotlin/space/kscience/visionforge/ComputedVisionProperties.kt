package space.kscience.visionforge

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.ObservableMutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.values.Value

private class ComputedVisionProperties(
    public val vision: Vision,
    public val rootName: Name,
    public val visionDescriptor: MetaDescriptor
) : ObservableMutableMeta by vision.meta {

    public val descriptor: MetaDescriptor? = visionDescriptor[rootName]

    override val items: Map<NameToken, ObservableMutableMeta>
        get() {
            val metaKeys = vision.meta.items.keys
            val descriptorKeys = descriptor?.children?.map { NameToken(it.key) } ?: emptySet()
            return (metaKeys + descriptorKeys).associateWith { getMeta(rootName + it) }
        }

    override var value: Value?
        get() {
            val inheritFlag = descriptor?.inherited ?: false
            val stylesFlag = descriptor?.usesStyles ?: true
            return vision.getPropertyValue(rootName, inheritFlag, stylesFlag, true)
        }
        set(value) {
            vision.meta.setValue(rootName, value)
        }

    override fun getMeta(name: Name): ObservableMutableMeta =
        ComputedVisionProperties(vision, rootName + name, visionDescriptor)

    override fun getOrCreate(name: Name): ObservableMutableMeta = getMeta(name)

    override fun toMeta(): Meta = this
}

/**
 * Compute property node based on inheritance and style information from the descriptor
 */
public fun Vision.computeProperties(descriptor: MetaDescriptor? = this.descriptor): ObservableMutableMeta =
    if (descriptor == null) meta else ComputedVisionProperties(this, Name.EMPTY, descriptor)

public fun Vision.computePropertyNode(name: Name, descriptor: MetaDescriptor? = this.descriptor): ObservableMutableMeta? =
    computeProperties(descriptor)[name]