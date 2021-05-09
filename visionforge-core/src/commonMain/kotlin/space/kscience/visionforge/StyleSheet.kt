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
public value class StyleSheet(private val owner: VisionGroup) {

    private val styleNode get() = owner.ownProperties[STYLESHEET_KEY].node

    public val items: Map<NameToken, Meta>? get() = styleNode?.items?.mapValues { it.value.node ?: Meta.EMPTY }

    public operator fun get(key: String): Meta? = owner.getStyle(key)

    /**
     * Define a style without notifying owner
     */
    public fun define(key: String, style: Meta?) {
        owner.setProperty(STYLESHEET_KEY + key, style)
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
    public operator fun set(key: String, builder: MetaBuilder.() -> Unit) {
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
        tokens.forEach { parent?.invalidateProperty(it) }
    }
    if (this is VisionGroup) {
        for (obj in this) {
            obj.styleChanged(key, oldStyle, newStyle)
        }
    }
}


/**
 * List of names of styles applied to this object. Order matters. Not inherited.
 */
public var Vision.styles: List<String>
    get() = ownProperties[Vision.STYLE_KEY]?.stringList ?: emptyList()
    set(value) {
        setProperty(Vision.STYLE_KEY, value)
    }

/**
 * A stylesheet for this group and its descendants. Stylesheet is not applied directly,
 * but instead is just a repository for named configurations.
 */
public val VisionGroup.styleSheet: StyleSheet get() = StyleSheet(this)

/**
 * Add style name to the list of styles to be resolved later. The style with given name does not necessary exist at the moment.
 */
public fun Vision.useStyle(name: String) {
    styles = (ownProperties[Vision.STYLE_KEY]?.stringList ?: emptyList()) + name
}


/**
 * Find a style with given name for given [Vision]. The style is not necessary applied to this [Vision].
 */
public tailrec fun Vision.getStyle(name: String): Meta? =
    ownProperties[StyleSheet.STYLESHEET_KEY + name].node ?: parent?.getStyle(name)

/**
 * Resolve an item in all style layers
 */
public fun Vision.getStyleItems(name: Name): List<MetaItem> = styles.mapNotNull {
    getStyle(it)[name]
}


