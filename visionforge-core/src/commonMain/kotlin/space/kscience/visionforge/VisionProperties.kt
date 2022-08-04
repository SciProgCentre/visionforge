package space.kscience.visionforge

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.values.Value

/**
 * A wrapper that emulates delegates reading and writing properties to Vision method
 */
internal class VisionProperties(
    val vision: Vision,
    val nodeName: Name,
    val visionDescriptor: MetaDescriptor? = vision.descriptor,
    val inherit: Boolean? = null,
    val useStyles: Boolean? = null,
) : MutableMeta {

    val descriptor: MetaDescriptor? by lazy { visionDescriptor?.get(nodeName) }

    override val items: Map<NameToken, MutableMeta>
        get() {
            val metaKeys = vision.meta.getMeta(nodeName)?.items?.keys ?: emptySet()
            val descriptorKeys = descriptor?.children?.map { NameToken(it.key) } ?: emptySet()
            val inheritFlag = descriptor?.inherited ?: inherit
            val stylesFlag = descriptor?.usesStyles ?: useStyles
            return (metaKeys + descriptorKeys).associateWith {
                VisionProperties(
                    vision,
                    nodeName + it,
                    visionDescriptor,
                    inheritFlag,
                    stylesFlag
                )
            }
        }

    override var value: Value?
        get() {
            val inheritFlag = descriptor?.inherited ?: inherit ?: false
            val stylesFlag = descriptor?.usesStyles ?: useStyles ?: true
            return vision.getPropertyValue(nodeName, inheritFlag, stylesFlag, true)
        }
        set(value) {
            vision.setPropertyValue(nodeName, value)
        }

    override fun getOrCreate(name: Name): MutableMeta = VisionProperties(
        vision,
        nodeName + name,
        visionDescriptor,
        inherit,
        useStyles
    )

    override fun setMeta(name: Name, node: Meta?) {
        vision.setProperty(nodeName + name, node)
    }

    override fun toString(): String = Meta.toString(this)
    override fun equals(other: Any?): Boolean = Meta.equals(this, other as? Meta)
    override fun hashCode(): Int = Meta.hashCode(this)
}

///**
// * Accessor to all vision properties
// */
//public fun Vision.computePropertyValues(
//    descriptor: MetaDescriptor? = this.descriptor,
//): MutableValueProvider = object : MutableValueProvider {
//    override fun getValue(name: Name): Value? = computeProperty(name, descriptor?.get(name))?.value
//
//    override fun setValue(name: Name, value: Value?) {
//        setProperty(name, value)
//    }
//}

