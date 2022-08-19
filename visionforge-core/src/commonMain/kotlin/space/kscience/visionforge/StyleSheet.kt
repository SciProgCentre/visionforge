package space.kscience.visionforge

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import kotlin.jvm.JvmInline

/**
 * A container for styles
 */
@JvmInline
public value class StyleSheet(private val owner: Vision) {

    private val styleNode: Meta get() = owner.properties.getProperty(STYLESHEET_KEY)

    public val items: Map<NameToken, Meta> get() = styleNode.items

    public operator fun get(key: String): Meta? = owner.getStyle(key)

    /**
     * Define a style without notifying owner
     */
    public fun define(key: String, style: Meta?) {
        owner.properties.setProperty(STYLESHEET_KEY + key, style)
    }

    /**
     * Set or clear the style
     */
    public operator fun set(key: String, style: Meta?) {
        val oldStyle = get(key)
        define(key, style)
        owner.styleChanged(key, oldStyle, style)
    }

    public inline operator fun invoke(block: StyleSheet.() -> Unit): Unit = this.block()

    /**
     * Create and set a style
     */
    public fun update(key: String, builder: MutableMeta.() -> Unit) {
        val newStyle = get(key)?.toMutableMeta()?.apply(builder) ?: Meta(builder)
        set(key, newStyle.seal())
    }

    public companion object {
        public val STYLESHEET_KEY: Name = "@stylesheet".asName()
    }
}

internal fun Vision.styleChanged(key: String, oldStyle: Meta?, newStyle: Meta?) {
    if (styles.contains(key)) {
        //TODO optimize set concatenation
        val tokens: Collection<Name> =
            ((oldStyle?.items?.keys ?: emptySet()) + (newStyle?.items?.keys ?: emptySet()))
                .map { it.asName() }
        tokens.forEach { parent?.properties?.invalidate(it) }
    }
    children?.forEach { _, vision ->
        vision.styleChanged(key, oldStyle, newStyle)
    }
}

/**
 * List of names of styles applied to this object. Order matters. Not inherited.
 */
public var Vision.styles: List<String>
    get() = properties.own?.getValue(Vision.STYLE_KEY)?.stringList ?: emptyList()
    set(value) {
        properties.setValue(Vision.STYLE_KEY, value.map { it.asValue() }.asValue())
    }

/**
 * A stylesheet for this group and its descendants. Stylesheet is not applied directly,
 * but instead is just a repository for named configurations.
 */
public val Vision.styleSheet: StyleSheet get() = StyleSheet(this)

/**
 * Add style name to the list of styles to be resolved later.
 * The style with given name does not necessary exist at the moment.
 */
public fun Vision.useStyle(name: String, notify: Boolean = true) {
    val newStyle = properties.own?.get(Vision.STYLE_KEY)?.value?.list?.plus(name.asValue()) ?: listOf(name.asValue())
    properties.setValue(Vision.STYLE_KEY, newStyle.asValue(), notify)
}


/**
 * Resolve a style with given name for given [Vision]. The style is not necessarily applied to this [Vision].
 */
public fun Vision.getStyle(name: String): Meta? =
    properties.own?.getMeta(StyleSheet.STYLESHEET_KEY + name) ?: parent?.getStyle(name)

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


