package space.kscience.visionforge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.values.ValueType
import space.kscience.visionforge.Vision.Companion.STYLE_KEY
import kotlin.jvm.Synchronized

/**
 * A full base implementation for a [Vision]
 */
@Serializable
@SerialName("vision.group")
public open class VisionGroup : AbstractVision(), MutableVisionGroup {

    override fun update(change: VisionChange) {
        change.children?.forEach { (name, change) ->
            when {
                change.delete -> children.set(name, null)
                change.vision != null -> children.set(name, change.vision)
                else -> children[name]?.update(change)
            }
        }
        change.properties?.let {
            updateProperties(it, Name.EMPTY)
        }
    }

    @SerialName("children")
    protected var _children: MutableVisionChildren? = null

    @Transient
    override val children: MutableVisionChildren = object : MutableVisionChildren {

        @Synchronized
        fun getOrCreateChildren(): MutableVisionChildren {
            if (_children == null) {
                _children = VisionChildrenImpl(emptyMap()).apply {
                    parent = this@VisionGroup
                }
            }
            return _children!!
        }

        override val parent: MutableVisionGroup get() = this@VisionGroup

        override val keys: Set<NameToken> get() = _children?.keys ?: emptySet()
        override val changes: Flow<Name> get() = _children?.changes ?: emptyFlow()

        override fun get(token: NameToken): Vision? = _children?.get(token)

        override fun set(token: NameToken, value: Vision?) {
            getOrCreateChildren()[token] = value
        }

        override fun set(name: Name?, child: Vision?) {
            getOrCreateChildren()[name] = child
        }

        override fun clear() {
            _children?.clear()
        }
    }

    override fun createGroup(): VisionGroup = VisionGroup()

    public companion object {
        public val descriptor: MetaDescriptor = MetaDescriptor {
            value(STYLE_KEY, ValueType.STRING) {
                multiple = true
            }
        }

        public fun Vision.updateProperties(item: Meta, at: Name = Name.EMPTY) {
            setPropertyValue(at, item.value)
            item.items.forEach { (token, item) ->
                updateProperties(item, at + token)
            }
        }

    }
}

//fun VisualObject.findStyle(styleName: Name): Meta? {
//    if (this is VisualGroup) {
//        val style = resolveStyle(styleName)
//        if (style != null) return style
//    }
//    return parent?.findStyle(styleName)
//}