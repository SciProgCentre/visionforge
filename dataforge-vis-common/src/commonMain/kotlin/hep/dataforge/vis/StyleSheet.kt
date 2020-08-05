@file:UseSerializers(MetaSerializer::class)

package hep.dataforge.vis

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

/**
 * A container for styles
 */
@Serializable
class StyleSheet private constructor(private val styleMap: MutableMap<String, Meta> = LinkedHashMap()) {
    @Transient
    internal var owner: VisualObject? = null

    constructor(owner: VisualObject) : this() {
        this.owner = owner
    }

    val items: Map<String, Meta> get() = styleMap

    operator fun get(key: String): Meta? {
        return styleMap[key] ?: owner?.parent?.styleSheet?.get(key)
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

    /**
     * Set or clear the style
     */
    operator fun set(key: String, style: Meta?) {
        val oldStyle = styleMap[key]
        define(key, style)
        owner?.styleChanged(key, oldStyle, style)
    }

    /**
     * Create and set a style
     */
    operator fun set(key: String, builder: MetaBuilder.() -> Unit) {
        val newStyle = get(key)?.edit(builder) ?: Meta(builder)
        set(key, newStyle.seal())
    }

    @Serializer(StyleSheet::class)
    companion object : KSerializer<StyleSheet> {
        private val mapSerializer = MapSerializer(String.serializer(), MetaSerializer)
        override val descriptor: SerialDescriptor get() = mapSerializer.descriptor


        override fun deserialize(decoder: Decoder): StyleSheet {
            val map = mapSerializer.deserialize(decoder)
            return StyleSheet(map as? MutableMap<String, Meta> ?: LinkedHashMap(map))
        }

        override fun serialize(encoder: Encoder, value: StyleSheet) {
            mapSerializer.serialize(encoder, value.items)
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
        for (obj in this) {
            obj.styleChanged(key, oldStyle, newStyle)
        }
    }
}