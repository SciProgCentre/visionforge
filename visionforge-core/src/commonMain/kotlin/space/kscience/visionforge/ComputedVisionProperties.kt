package space.kscience.visionforge

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.values.MutableValueProvider
import space.kscience.dataforge.values.Value

private class ComputedVisionProperties(
    val vision: Vision,
    val pathName: Name,
    val visionDescriptor: MetaDescriptor,
    val parentInheritFlag: Boolean?,
    val parentStylesFlag: Boolean?
) : Meta {

    val descriptor: MetaDescriptor? by lazy { visionDescriptor[pathName] }

    override val items: Map<NameToken, Meta>
        get() {
            val metaKeys = vision.meta.getMeta(pathName)?.items?.keys ?: emptySet()
            val descriptorKeys = descriptor?.children?.map { NameToken(it.key) } ?: emptySet()
            val inheritFlag = descriptor?.inherited ?: parentInheritFlag
            val stylesFlag = descriptor?.usesStyles ?: parentStylesFlag
            return (metaKeys + descriptorKeys).associateWith {
                ComputedVisionProperties(
                    vision,
                    pathName + it,
                    visionDescriptor,
                    inheritFlag,
                    stylesFlag
                )
            }
        }

    override val value: Value?
        get() {
            val inheritFlag = descriptor?.inherited ?: parentInheritFlag ?: false
            val stylesFlag = descriptor?.usesStyles ?: parentStylesFlag ?: true
            return vision.getPropertyValue(pathName, inheritFlag, stylesFlag, true)
        }

    override fun toString(): String = Meta.toString(this)
    override fun equals(other: Any?): Boolean = Meta.equals(this, other as? Meta)
    override fun hashCode(): Int = Meta.hashCode(this)
}

/**
 * Compute property node based on inheritance and style information from the descriptor
 */
public fun Vision.computeProperties(descriptor: MetaDescriptor? = this.descriptor): Meta =
    if (descriptor == null) meta else ComputedVisionProperties(this, Name.EMPTY, descriptor, null, null)

public fun Vision.computePropertyNode(
    name: Name,
    descriptor: MetaDescriptor? = this.descriptor
): Meta? = computeProperties(descriptor)[name]

/**
 * Compute the property based on the provided value descriptor. By default, use Vision own descriptor
 */
public fun Vision.computeProperty(name: Name, valueDescriptor: MetaDescriptor? = descriptor?.get(name)): Value? {
    val inheritFlag = valueDescriptor?.inherited ?: false
    val stylesFlag = valueDescriptor?.usesStyles ?: true
    return getPropertyValue(name, inheritFlag, stylesFlag)
}

/**
 * Accessor to all vision properties
 */
public fun Vision.computePropertyValues(
    descriptor: MetaDescriptor? = this.descriptor
): MutableValueProvider = object : MutableValueProvider {
    override fun getValue(name: Name): Value? = computeProperty(name, descriptor?.get(name))

    override fun setValue(name: Name, value: Value?) {
        setProperty(name, value)
    }
}

