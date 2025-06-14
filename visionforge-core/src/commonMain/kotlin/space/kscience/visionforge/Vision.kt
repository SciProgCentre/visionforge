package space.kscience.visionforge

import kotlinx.coroutines.flow.Flow
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.context.warn
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.Described
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.misc.DfType
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.parseAsName
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.provider.Provider
import space.kscience.visionforge.SimpleVisionGroup.Companion.updateProperties
import space.kscience.visionforge.Vision.Companion.STYLESHEET_KEY
import space.kscience.visionforge.Vision.Companion.TYPE
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * A root type for display hierarchy
 */
@DfType(TYPE)
public interface Vision : Described, Provider {

    /**
     * The parent object of this one. If null, this one is a root.
     */
    public var parent: Vision?

    override val descriptor: MetaDescriptor?

    /**
     * Owner [VisionManager]. Used to define coroutine scope a serialization
     */
    public val manager: VisionManager? get() = parent?.manager

    public val properties: Meta

    /**
     * A flow of outgoing events from this vision
     */
    public val eventFlow: Flow<VisionEvent>

    /**
     * Receive and process a generic [VisionEvent].
     */
    public suspend fun receiveEvent(event: VisionEvent) {
        manager?.logger?.warn { "Undispatched event: $event" }
    }

    /**
     * Read a property, taking into account inherited properties and styles if necessary
     */
    public fun readProperty(
        name: Name,
        inherited: Boolean = isInheritedProperty(name),
        useStyles: Boolean = isStyledProperty(name),
    ): Meta? {
        val ownMeta = properties[name]
        val styleMeta = if (useStyles) getStyleProperty(name) else null
        val inheritMeta = if (inherited) parent?.readProperty(name, inherited, useStyles) else null
        val defaultMeta = descriptor?.defaultNode?.get(name)
        val listOfMeta = listOf(ownMeta, styleMeta, inheritMeta, defaultMeta)

        return if (listOfMeta.all { it == null }) null else Laminate(listOfMeta)
    }

    override val defaultTarget: String get() = VISION_PROPERTY_TARGET

    override fun content(target: String): Map<Name, Any> = if (target == VISION_PROPERTY_TARGET) {
        readProperties().items.entries.associate { it.key.asName() to it.value }
    } else {
        emptyMap()
    }

    public companion object {
        public const val TYPE: String = "vision"

        public const val VISION_PROPERTY_TARGET: String = "property"

        public val STYLE_KEY: Name = "@style".asName()
        public val STYLESHEET_KEY: Name = "@stylesheet".asName()
        public const val STYLE_TARGET: String = "style"

        public val VISIBLE_KEY: Name = "visible".asName()

        @OptIn(ExperimentalUuidApi::class)
        public fun randomId(): String = Uuid.random().toHexString()
    }
}

internal fun Vision.isInheritedProperty(name: Name): Boolean = descriptor?.get(name)?.inherited == true
internal fun Vision.isInheritedProperty(name: String): Boolean = descriptor?.get(name)?.inherited == true
internal fun Vision.isStyledProperty(name: Name): Boolean = descriptor?.get(name)?.usesStyles != false
internal fun Vision.isStyledProperty(name: String): Boolean = descriptor?.get(name)?.usesStyles != false

public fun Vision.readProperty(
    name: String,
    inherited: Boolean = isInheritedProperty(name),
    useStyles: Boolean = isStyledProperty(name),
): Meta? = readProperty(name.parseAsName(), inherited, useStyles)


public fun Vision.readProperties(
    inherited: Boolean = false,
    useStyles: Boolean = true,
): Meta = readProperty(Name.EMPTY, inherited, useStyles) ?: Meta.EMPTY

public interface MutableVision : Vision {
    override val properties: MutableMeta

    /**
     * Receive and process a generic [VisionEvent].
     */
    override suspend fun receiveEvent(event: VisionEvent) {
        if (event is VisionChange) {
            if (event.children?.isNotEmpty() == true) {
                error("Received vision group change event, but $this Vision does not handle children changes")
            }
            event.properties?.let {
                updateProperties(it, Name.EMPTY)
            }
        } else super.receiveEvent(event)
    }

    public fun mutableProperty(
        name: Name,
        inherited: Boolean = isInheritedProperty(name),
        useStyles: Boolean = isStyledProperty(name),
    ): MutableMeta = properties.getOrCreate(name).withDefault { suffix ->
        val propertyName = name + suffix
        if (useStyles) getStyleProperty(propertyName)?.let { return@withDefault it }
        if (inherited) parent?.readProperty(propertyName, inherited, useStyles)?.let { return@withDefault it }

        descriptor?.defaultNode?.get(propertyName)
    }
}

public fun MutableVision.properties(block: MutableMeta.() -> Unit) {
    properties.apply(block)
}

public fun MutableVision.writeProperties(
    inherited: Boolean = false,
    useStyles: Boolean = true,
): MutableMeta = mutableProperty(Name.EMPTY, inherited, useStyles)

/**
 * Control visibility of the element
 */
public var MutableVision.visible: Boolean?
    get() = properties.getValue(Vision.VISIBLE_KEY)?.boolean
    set(value) {
        properties.setValue(Vision.VISIBLE_KEY, value?.asValue())
    }


public val Vision.styleSheet: Meta?
    get() = properties[STYLESHEET_KEY]

/**
 * List of style names applied to this object. Order matters. Not inherited.
 */
public val Vision.styles: List<String>
    get() = properties[Vision.STYLE_KEY]?.stringList ?: emptyList()


public var MutableVision.styles: List<String>
    get() = properties[Vision.STYLE_KEY]?.stringList ?: emptyList()
    set(value) {
        properties[Vision.STYLE_KEY] = value.map { it.asValue() }.asValue()
    }

public fun MutableVision.setStyle(styleName: String, style: Meta) {
    properties[STYLESHEET_KEY + styleName] = style
}

/**
 * Define or modify a style with given [styleName]. The style is not used immediately. Call [useStyle] to enable it for this vision
 */
public fun MutableVision.updateStyle(styleName: String, block: MutableMeta.() -> Unit) {
    properties.getOrCreate(STYLESHEET_KEY + styleName).block()
}

/**
 * Add style name to the list of styles to be resolved later.
 * The style with given name does not necessary exist at the moment.
 */
public fun MutableVision.useStyle(styleName: String) {
    val newStyleList =
        properties[Vision.STYLE_KEY]?.value?.list?.plus(styleName.asValue()) ?: listOf(styleName.asValue())
    properties.setValue(Vision.STYLE_KEY, newStyleList.asValue())
}

/**
 * Resolve a style with given name for given [Vision]. The style is not necessarily applied to this [Vision].
 */
public fun Vision.getStyle(name: String): Meta? =
    readProperty(STYLESHEET_KEY + name, inherited = true, useStyles = false)
//properties[STYLESHEET_KEY + name] ?: parent?.getStyle(name)

/**
 * Resolve a property from all styles
 */
public fun Vision.getStyleProperty(name: Name): Meta? = styles.firstNotNullOfOrNull { getStyle(it)?.get(name) }

/**
 * Resolve an item in all style layers
 */
public fun Vision.getStyleNodes(name: Name): List<Meta> = styles.mapNotNull {
    getStyle(it)?.get(name)
}