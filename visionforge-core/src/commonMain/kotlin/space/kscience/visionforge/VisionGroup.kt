package space.kscience.visionforge

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.values.ValueType
import space.kscience.visionforge.Vision.Companion.STYLE_KEY
import kotlin.js.JsName


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
@Serializable
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
    protected var childrenInternal: MutableMap<NameToken, Vision>? = null


    init {
        childrenInternal?.forEach { it.value.parent = this }
    }

    override val children: MutableVisionChildren by lazy {
        object : VisionChildrenImpl(this) {
            override var items: MutableMap<NameToken, Vision>?
                get() = this@AbstractVisionGroup.childrenInternal
                set(value) {
                    this@AbstractVisionGroup.childrenInternal = value
                }
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