package space.kscience.visionforge

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.isEmpty
import space.kscience.dataforge.values.Value
import space.kscience.visionforge.AbstractVisionGroup.Companion.updateProperties
import kotlin.jvm.Synchronized


public abstract class AbstractVision : Vision {

    @Transient
    override var parent: Vision? = null

    @SerialName("properties")
    internal var _properties: MutableMeta? = null

    protected open val defaultProperties: Meta? get() = descriptor?.defaultNode

    @Transient
    final override val properties: MutableVisionProperties = object : MutableVisionProperties {
        override val descriptor: MetaDescriptor? get() = this@AbstractVision.descriptor
        override val default: Meta? get() = defaultProperties

        @Synchronized
        private fun getOrCreateProperties(): MutableMeta {
            if (_properties == null) {
                //TODO check performance issues
                val newProperties = MutableMeta()
                _properties = newProperties
            }
            return _properties!!
        }

        override val raw: Meta? get() = _properties

        override fun getValue(
            name: Name,
            inherit: Boolean,
            includeStyles: Boolean,
        ): Value? {
            raw?.get(name)?.value?.let { return it }
            if (includeStyles) {
                getStyleProperty(name)?.value?.let { return it }
            }
            if (inherit) {
                parent?.properties?.getValue(name, inherit, includeStyles)?.let { return it }
            }
            return default?.get(name)?.value
        }

        override fun set(name: Name, node: Meta?) {
            //TODO check old value?
            if (name.isEmpty()) {
                _properties = node?.asMutableMeta()
            } else if (node == null) {
                _properties?.setMeta(name, node)
            } else {
                getOrCreateProperties().setMeta(name, node)
            }
            invalidate(name)
        }

        override fun setValue(name: Name, value: Value?) {
            //TODO check old value?
            if (value == null) {
                _properties?.getMeta(name)?.value = null
            } else {
                getOrCreateProperties().setValue(name, value)
            }
            invalidate(name)
        }

        @Transient
        private val _changes = MutableSharedFlow<Name>()
        override val changes: SharedFlow<Name> get() = _changes

        override fun invalidate(propertyName: Name) {
            if (propertyName == Vision.STYLE_KEY) {
                styles.asSequence()
                    .mapNotNull { getStyle(it) }
                    .flatMap { it.items.asSequence() }
                    .distinctBy { it.key }
                    .forEach {
                        invalidate(it.key.asName())
                    }
            }
            manager.context.launch {
                _changes.emit(propertyName)
            }
        }

    }

    override val descriptor: MetaDescriptor? get() = null


    override fun update(change: VisionChange) {
        change.properties?.let {
            updateProperties(it, Name.EMPTY)
        }
    }
}