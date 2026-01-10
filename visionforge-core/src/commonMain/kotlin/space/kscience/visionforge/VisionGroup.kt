package space.kscience.visionforge

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.ValueType
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.SimpleVisionGroup.Companion.updateProperties
import space.kscience.visionforge.Vision.Companion.STYLE_KEY
import space.kscience.visionforge.Vision.Companion.VISION_PROPERTY_TARGET


public interface VisionGroup<out V : Vision> : Vision, VisionContainer<V> {

    public val visions: Map<NameToken, V>

    override fun getVision(token: NameToken): V? = visions[token]

    override val defaultTarget: String get() = VISION_CHILD_TARGET

    override val defaultChainTarget: String get() = VISION_PROPERTY_TARGET

    override fun content(target: String): Map<Name, Any> = when (target) {
        VISION_PROPERTY_TARGET -> readProperties().items.entries.associate { it.key.asName() to it.value }
        VISION_CHILD_TARGET -> visions.mapKeys { it.key.asName() }
        else -> emptyMap()
    }

    public companion object {
        public const val VISION_CHILD_TARGET: String = "vision"
    }
}

///**
// * An event that indicates that child property value has been invalidated
// */
//public data class VisionGroupPropertyChangedEvent(
//    public val source: VisionGroup<*>,
//    public val childName: Name,
//    public val propertyName: Name
//) : VisionEvent

@VisionBuilder
public interface MutableVisionGroup<V : Vision> : VisionGroup<V>, MutableVision, MutableVisionContainer<V> {

    /**
     * This method tries to convert a [vision] to the typed vision handled by this [MutableVisionGroup].
     * Return null if conversion is failed.
     */
    public fun convertVisionOrNull(vision: Vision): V?

    override suspend fun receiveEvent(event: VisionEvent) {
        if (event is VisionChange) {
            event.properties?.let {
                updateProperties(it, Name.EMPTY)
            }
            event.children?.forEach { (childName, change) ->
                when {
                    change.vision == NullVision -> setVision(childName, null)

                    change.vision != null -> setVision(
                        childName,
                        convertVisionOrNull(change.vision) ?: error("Can't convert ${change.vision}")
                    )

                    else -> getVision(childName)?.receiveEvent(change)
                }
            }
        } else {
            super<MutableVision>.receiveEvent(event)
        }
    }
}

/**
 * A simple vision group that just holds children. Nothing else.
 */
@Serializable
@SerialName("group")
public class SimpleVisionGroup : AbstractVision(), MutableVisionGroup<Vision> {

    @Serializable
    @SerialName("children")
    private val _items = mutableMapOf<NameToken, Vision>()

    // ensure proper children links after deserialization
    init {
        _items.forEach { it.value.parent = this }
    }

    override val visions: Map<NameToken, Vision> get() = _items

    override fun convertVisionOrNull(vision: Vision): Vision = vision

    override fun setVision(token: NameToken, vision: Vision?) {
        if (vision == null) {
            _items.remove(token)
        } else {
            _items[token] = vision
            vision.parent = this
        }
        emitEvent(VisionGroupCompositionChangedEvent(token, vision))
    }

    public companion object {
        public val descriptor: MetaDescriptor = MetaDescriptor {
            value(STYLE_KEY, ValueType.STRING) {
                multiple = true
            }
        }

        public fun MutableVision.updateProperties(item: Meta, name: Name = Name.EMPTY) {
            properties.setValue(name, item.value)
            item.items.forEach { (token, item) ->
                updateProperties(item, name + token)
            }
        }

    }
}

public inline fun MutableVisionContainer<Vision>.group(
    name: NameToken? = null,
    builder: SimpleVisionGroup.() -> Unit = {},
): SimpleVisionGroup = SimpleVisionGroup().also {
    setVision(name ?: MutableVisionContainer.generateID(), it)
}.apply(builder)

/**
 * Define a group with given [token], attach it to this parent and return it.
 */
public inline fun MutableVisionContainer<Vision>.group(
    token: String,
    builder: SimpleVisionGroup.() -> Unit = {},
): SimpleVisionGroup = group(NameToken.parse(token), builder)

public fun VisionGroup(
    parent: Vision? = null,
    block: MutableVisionGroup<Vision>.() -> Unit
): VisionGroup<Vision> = SimpleVisionGroup().apply(block).also {
    it.parent = parent
}

public fun MutableVisionGroup(
    parent: Vision? = null,
    block: MutableVisionGroup<Vision>.() -> Unit
): MutableVisionGroup<Vision> = SimpleVisionGroup().apply(block).also {
    it.parent = parent
}

//fun VisualObject.findStyle(styleName: Name): Meta? {
//    if (this is VisualGroup) {
//        val style = resolveStyle(styleName)
//        if (style != null) return style
//    }
//    return parent?.findStyle(styleName)
//}