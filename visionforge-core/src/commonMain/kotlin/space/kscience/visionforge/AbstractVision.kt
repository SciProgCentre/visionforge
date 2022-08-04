package space.kscience.visionforge

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.asMutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.isEmpty
import space.kscience.dataforge.values.Value
import space.kscience.visionforge.VisionGroup.Companion.updateProperties
import kotlin.jvm.Synchronized

@Serializable
public abstract class AbstractVision : Vision {

    @Transient
    override var parent: Vision? = null

    protected var properties: MutableMeta? = null

    override val meta: Meta get() = properties ?: Meta.EMPTY

    @Synchronized
    private fun getOrCreateProperties(): MutableMeta {
        if (properties == null) {
            //TODO check performance issues
            val newProperties = MutableMeta()
            properties = newProperties
        }
        return properties!!
    }

    @Transient
    private val _propertyChanges = MutableSharedFlow<Name>()
    override val propertyChanges: SharedFlow<Name> get() = _propertyChanges

    override fun getPropertyValue(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): Value? {
        properties?.get(name)?.value?.let { return it }
        if (includeStyles) {
            getStyleProperty(name)?.value?.let { return it }
        }
        if (inherit) {
            parent?.getPropertyValue(name, inherit, includeStyles, includeDefaults)?.let { return it }
        }
        if (includeDefaults) {
            descriptor?.defaultNode?.get(name)?.value?.let { return it }
        }
        return null
    }

    override fun setProperty(name: Name, node: Meta?) {
        //TODO check old value?
        if (name.isEmpty()) {
            properties = node?.asMutableMeta()
        } else if (node == null) {
            properties?.setMeta(name, node)
        } else {
            getOrCreateProperties().setMeta(name, node)
        }
        invalidateProperty(name)
    }

    override fun setPropertyValue(name: Name, value: Value?) {
        //TODO check old value?
        if (value == null) {
            properties?.getMeta(name)?.value = null
        } else {
            getOrCreateProperties().setValue(name, value)
        }
        invalidateProperty(name)
    }

    override val descriptor: MetaDescriptor? get() = null

    override fun invalidateProperty(propertyName: Name) {
        if (propertyName == Vision.STYLE_KEY) {
            styles.asSequence()
                .mapNotNull { getStyle(it) }
                .flatMap { it.items.asSequence() }
                .distinctBy { it.key }
                .forEach {
                    invalidateProperty(it.key.asName())
                }
        }
        manager.context.launch {
            _propertyChanges.emit(propertyName)
        }
    }

    override fun update(change: VisionChange) {
        change.properties?.let {
            updateProperties(it, Name.EMPTY)
        }
    }
}