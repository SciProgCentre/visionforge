@file:UseSerializers(MetaSerializer::class)

package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.values.asValue
import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

/**
 * A container for styles
 */
@Serializable
class StyleSheet private constructor(private val styleMap: MutableMap<String, Meta> = LinkedHashMap()) {
    @Transient
    internal var owner: Vision? = null

    constructor(owner: Vision) : this() {
        this.owner = owner
    }

    val items: Map<String, Meta> get() = styleMap


    private fun Vision.styleChanged(key: String, oldStyle: Meta?, newStyle: Meta?) {
        if (styles.contains(key)) {
            //TODO optimize set concatenation
            val tokens: Collection<Name> = ((oldStyle?.items?.keys ?: emptySet()) + (newStyle?.items?.keys ?: emptySet()))
                .map { it.asName() }
            tokens.forEach { parent?.propertyChanged(it) }
        }
        if (this is VisionGroup) {
            for (obj in this) {
                obj.styleChanged(key, oldStyle, newStyle)
            }
        }
    }

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


/**
 * List of names of styles applied to this object. Order matters. Not inherited
 */
var Vision.styles: List<String>
    get() = properties?.get(Vision.STYLE_KEY).stringList
    set(value) {
        setItem(Vision.STYLE_KEY,value.map { it.asValue() }.asValue())
    }

/**
 * Add style name to the list of styles to be resolved later. The style with given name does not necessary exist at the moment.
 */
fun Vision.useStyle(name: String) {
    styles = styles + name
}