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
import kotlin.js.JsName
import kotlin.jvm.Synchronized


public interface VisionGroup : Vision {
    public val children: VisionChildren
}

public interface MutableVisionGroup : VisionGroup {

    override val children: MutableVisionChildren

    public fun createGroup(): MutableVisionGroup
}

public val Vision.children: VisionChildren? get() = (this as? VisionGroup)?.children

/**
 * A full base implementation for a [Vision]
 */
public abstract class AbstractVisionGroup : AbstractVision(), MutableVisionGroup {

    override fun update(change: VisionChange) {
        change.children?.forEach { (name, change) ->
            when {
                change.delete -> children[name] = null
                change.vision != null -> children[name] = change.vision
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
                    group = this@AbstractVisionGroup
                }
            }
            return _children!!
        }

        override val group: MutableVisionGroup get() = this@AbstractVisionGroup

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

    abstract override fun createGroup(): AbstractVisionGroup

    public companion object {
        public val descriptor: MetaDescriptor = MetaDescriptor {
            value(STYLE_KEY, ValueType.STRING) {
                multiple = true
            }
        }

        public fun Vision.updateProperties(item: Meta, name: Name = Name.EMPTY) {
            properties.setValue(name, item.value)
            item.items.forEach { (token, item) ->
                updateProperties(item, name + token)
            }
        }

    }
}

/**
 * A simple vision group that just holds children. Nothing else.
 */
@Serializable
@SerialName("vision.group")
public class SimpleVisionGroup : AbstractVisionGroup() {
    override fun createGroup(): SimpleVisionGroup = SimpleVisionGroup()
}

@JsName("createVisionGroup")
public fun VisionGroup(): VisionGroup = SimpleVisionGroup()

//fun VisualObject.findStyle(styleName: Name): Meta? {
//    if (this is VisualGroup) {
//        val style = resolveStyle(styleName)
//        if (style != null) return style
//    }
//    return parent?.findStyle(styleName)
//}