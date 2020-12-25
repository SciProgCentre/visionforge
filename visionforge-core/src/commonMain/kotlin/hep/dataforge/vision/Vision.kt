package hep.dataforge.vision

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.MutableItemProvider
import hep.dataforge.meta.descriptors.Described
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.meta.descriptors.get
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.toName
import hep.dataforge.provider.Type
import hep.dataforge.vision.Vision.Companion.TYPE
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.Transient

/**
 * A root type for display hierarchy
 */
@Type(TYPE)
public interface Vision : Described {

    /**
     * The parent object of this one. If null, this one is a root.
     */
    @Transient
    public var parent: VisionGroup?

    /**
     * Properties belonging to this [Vision] potentially including artificial properties
     */
    @Transient
    public val meta: Meta

    /**
     * A coroutine scope for asynchronous calls and locks
     */
    public val scope: CoroutineScope get() = parent?.scope ?: GlobalScope

    /**
     * A fast accessor method to get own property (no inheritance or styles).
     * Should be equivalent to `getProperty(name,false,false,false)`.
     */
    public fun getOwnProperty(name: Name): MetaItem?

    /**
     * Get property.
     * @param inherit toggles parent node property lookup. Null means inference from descriptor. Default is false.
     * @param includeStyles toggles inclusion of. Null means inference from descriptor. Default is true.
     */
    public fun getProperty(
        name: Name,
        inherit: Boolean = false,
        includeStyles: Boolean = true,
        includeDefaults: Boolean = true,
    ): MetaItem?


    /**
     * Set the property value
     */
    public fun setProperty(name: Name, item: MetaItem?, notify: Boolean = true)

    /**
     * Subscribe on property updates. The subscription is bound to the given [scope] and canceled when the scope is canceled
     */
    public fun onPropertyChange(scope: CoroutineScope, callback: suspend (Name) -> Unit)

    /**
     * Flow of property invalidation events. It does not contain property values after invalidation since it is not clear
     * if it should include inherited properties etc.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    public val propertyChanges: Flow<Name>
        get() = callbackFlow<Name> {
            coroutineScope {
                onPropertyChange(this) {
                    send(it)
                }
                awaitClose { cancel() }
            }
        }


    /**
     * Notify all listeners that a property has been changed and should be invalidated
     */
    public suspend fun notifyPropertyChanged(propertyName: Name): Unit

    /**
     * Update this vision using a dif represented by [VisionChange].
     */
    public fun update(change: VisionChange)

    override val descriptor: NodeDescriptor?

    public companion object {
        public const val TYPE: String = "vision"
        public val STYLE_KEY: Name = "@style".asName()

        public val VISIBLE_KEY: Name = "visible".asName()
    }
}

public fun Vision.asyncNotifyPropertyChange(propertyName: Name) {
    scope.launch {
        notifyPropertyChanged(propertyName)
    }
}

/**
 * Own properties, excluding inheritance, styles and descriptor
 */
public val Vision.ownProperties: MutableItemProvider
    get() = object : MutableItemProvider {
        override fun getItem(name: Name): MetaItem? = getOwnProperty(name)
        override fun setItem(name: Name, item: MetaItem?): Unit = setProperty(name, item)
    }


/**
 * Convenient accessor for all properties of a vision.
 * @param inherit - inherit property value from the parent by default. If null, inheritance is inferred from descriptor
 */
public fun Vision.allProperties(
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
    includeDefaults: Boolean = true,
): MutableItemProvider = object : MutableItemProvider {
    override fun getItem(name: Name): MetaItem? = getProperty(
        name,
        inherit = inherit ?: (descriptor?.get(name)?.inherited != false),
        includeStyles = includeStyles ?: (descriptor?.get(name)?.usesStyles == true),
        includeDefaults = includeDefaults
    )

    override fun setItem(name: Name, item: MetaItem?): Unit = setProperty(name, item)
}

/**
 * Get [Vision] property using key as a String
 */
public fun Vision.getProperty(
    key: String,
    inherit: Boolean = false,
    includeStyles: Boolean = true,
    includeDefaults: Boolean = true,
): MetaItem? = getProperty(key.toName(), inherit, includeStyles, includeDefaults)

/**
 * A convenience method to pair [getProperty]
 */
public fun Vision.setProperty(key: Name, item: Any?) {
    setProperty(key, MetaItem.of(item))
}

/**
 * A convenience method to pair [getProperty]
 */
public fun Vision.setProperty(key: String, item: Any?) {
    setProperty(key.toName(), MetaItem.of(item))
}
