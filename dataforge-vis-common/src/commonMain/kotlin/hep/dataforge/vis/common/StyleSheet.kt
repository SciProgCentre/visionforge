@file:UseSerializers(MetaSerializer::class)

package hep.dataforge.vis.common

import hep.dataforge.io.serialization.MetaSerializer
import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import kotlinx.serialization.*

/**
 * A container for styles
 */
@Serializable
class StyleSheet() {
    @Transient
    internal var owner: VisualObject? = null

    constructor(owner: VisualObject) : this() {
        this.owner = owner
    }

    private val styleMap = HashMap<String, Meta>()

    val items: Map<String, Meta> get() = styleMap

    operator fun get(key: String): Meta? {
        return styleMap[key] ?: (owner?.parent as? VisualGroup)?.styleSheet?.get(key)
    }

    /**
     * Define a style without notifying owner
     */
    fun define(key: String, style: Meta?) {
        if (style == null) {
            styleMap.remove(key)
        } else {
            styleMap[key] = style
        }
    }

    operator fun set(key: String, style: Meta?) {
        val oldStyle = styleMap[key]
        define(key, style)
        owner?.styleChanged(key, oldStyle, style)
    }

    operator fun set(key: String, builder: MetaBuilder.() -> Unit) {
        val newStyle = get(key)?.let { buildMeta(it, builder) } ?: buildMeta(builder)
        set(key, newStyle.seal())
    }

    companion object: KSerializer<StyleSheet>{
        override val descriptor: SerialDescriptor
            get() = TODO("Not yet implemented")

        override fun deserialize(decoder: Decoder): StyleSheet {
            TODO("Not yet implemented")
        }

        override fun serialize(encoder: Encoder, obj: StyleSheet) {
            TODO("Not yet implemented")
        }

    }
}

private fun VisualObject.styleChanged(key: String, oldStyle: Meta?, newStyle: Meta?) {
    if (styles.contains(key)) {
        //TODO optimize set concatenation
        val tokens: Collection<Name> = ((oldStyle?.items?.keys ?: emptySet()) + (newStyle?.items?.keys ?: emptySet()))
            .map { it.asName() }
        tokens.forEach { parent?.propertyChanged(it, oldStyle?.get(it), newStyle?.get(it)) }
    }
    if (this is VisualGroup) {
        this.forEach { it.styleChanged(key, oldStyle, newStyle) }
    }
}