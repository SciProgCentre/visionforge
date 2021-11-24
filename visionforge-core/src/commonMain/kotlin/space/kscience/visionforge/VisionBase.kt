package space.kscience.visionforge

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.ObservableMutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.*
import space.kscience.dataforge.values.Value
import space.kscience.dataforge.values.ValueType
import space.kscience.visionforge.Vision.Companion.STYLE_KEY
import kotlin.jvm.Synchronized

internal data class MetaListener(
    val owner: Any? = null,
    val callback: Meta.(name: Name) -> Unit,
)

/**
 * A full base implementation for a [Vision]
 * @param parent the parent object for this vision. Could've set later. Not serialized.
 */
@Serializable
@SerialName("vision")
public open class VisionBase(
    @Transient override var parent: VisionGroup? = null,
    protected var properties: MutableMeta? = null
) : Vision {

    @Synchronized
    protected fun getOrCreateProperties(): MutableMeta {
        if (properties == null) {
            val newProperties = MutableMeta()
            properties = newProperties
        }
        return properties!!
    }

    @Transient
    private val listeners: MutableList<MetaListener> = mutableListOf()

    private inner class VisionProperties(val pathName: Name) : ObservableMutableMeta {

        override val items: Map<NameToken, ObservableMutableMeta>
            get() = properties?.get(pathName)?.items?.mapValues { entry ->
                VisionProperties(pathName + entry.key)
            } ?: emptyMap()

        override var value: Value?
            get() = properties?.get(pathName)?.value
            set(value) {
                val oldValue = properties?.get(pathName)?.value
                getOrCreateProperties().setValue(pathName, value)
                if (oldValue != value) {
                    invalidate(Name.EMPTY)
                }
            }

        override fun getOrCreate(name: Name): ObservableMutableMeta = VisionProperties(pathName + name)

        override fun setMeta(name: Name, node: Meta?) {
            getOrCreateProperties().setMeta(pathName + name, node)
            invalidate(name)
        }

        @DFExperimental
        override fun attach(name: Name, node: ObservableMutableMeta) {
            val ownProperties = getOrCreateProperties()
            if (ownProperties is ObservableMutableMeta) {
                ownProperties.attach(pathName + name, node)
            } else {
                ownProperties.setMeta(pathName + name, node)
                node.onChange(this) { childName ->
                    ownProperties.setMeta(pathName + name + childName, this[childName])
                }
            }
        }

        override fun invalidate(name: Name) {
            invalidateProperty(pathName + name)
        }

        @Synchronized
        override fun onChange(owner: Any?, callback: Meta.(name: Name) -> Unit) {
            if (pathName.isEmpty()) {
                listeners.add((MetaListener(owner, callback)))
            } else {
                listeners.add(MetaListener(owner) { name ->
                    if (name.startsWith(pathName)) {
                        (this@MetaListener[pathName] ?: Meta.EMPTY).callback(name.removeHeadOrNull(pathName)!!)
                    }
                })
            }
        }

        @Synchronized
        override fun removeListener(owner: Any?) {
            listeners.removeAll { it.owner === owner }
        }

        override fun toString(): String = Meta.toString(this)
        override fun equals(other: Any?): Boolean = Meta.equals(this, other as? Meta)
        override fun hashCode(): Int = Meta.hashCode(this)
    }

    override val meta: ObservableMutableMeta get() = VisionProperties(Name.EMPTY)

    override fun getPropertyValue(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): Value? {
        properties?.get(name)?.value?.let { return it }
        if (includeStyles) {
            getStyleProperty(name)?.let { return it }
        }
        if (inherit) {
            parent?.getPropertyValue(name, inherit, includeStyles, includeDefaults)?.let { return it }
        }
        if (includeDefaults) {
            descriptor?.defaultNode?.get(name)?.value.let { return it }
        }
        return null
    }

    override val descriptor: MetaDescriptor? get() = null


    override fun invalidateProperty(propertyName: Name) {
        if (propertyName == STYLE_KEY) {
            styles.mapNotNull { getStyle(it) }.asSequence()
                .flatMap { it.items.asSequence() }
                .distinctBy { it.key }
                .forEach {
                    invalidateProperty(it.key.asName())
                }
        }
        listeners.forEach { it.callback(properties ?: Meta.EMPTY, propertyName) }
    }

    override fun update(change: VisionChange) {
        change.properties?.let {
            updateProperties(Name.EMPTY, it)
        }
    }

    public companion object {
        public val descriptor: MetaDescriptor = MetaDescriptor {
            value(STYLE_KEY, ValueType.STRING) {
                multiple = true
            }
        }

        public fun Vision.updateProperties(at: Name, item: Meta) {
            meta.setValue(at, item.value)
            item.items.forEach { (token, item) ->
                updateProperties(at + token, item)
            }
        }

    }
}

//fun VisualObject.findStyle(styleName: Name): Meta? {
//    if (this is VisualGroup) {
//        val style = resolveStyle(styleName)
//        if (style != null) return style
//    }
//    return parent?.findStyle(styleName)
//}