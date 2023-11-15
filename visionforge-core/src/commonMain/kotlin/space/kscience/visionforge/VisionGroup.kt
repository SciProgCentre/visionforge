package space.kscience.visionforge

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.ValueType
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.parseAsName
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.AbstractVisionGroup.Companion.updateProperties
import space.kscience.visionforge.Vision.Companion.STYLE_KEY


public interface VisionGroup : Vision {
    public val children: VisionChildren

    override fun receiveChange(change: VisionChange) {
        change.children?.forEach { (name, change) ->
            if (change.vision != null || change.vision == NullVision) {
                error("VisionGroup is read-only")
            } else {
                children.getChild(name)?.receiveChange(change)
            }
        }
        change.properties?.let {
            updateProperties(it, Name.EMPTY)
        }
    }
}

public interface MutableVisionGroup : VisionGroup {

    override val children: MutableVisionChildren

    public fun createGroup(): MutableVisionGroup

    override fun receiveChange(change: VisionChange) {
        change.children?.forEach { (name, change) ->
            when {
                change.vision == NullVision -> children.setChild(name, null)
                change.vision != null -> children.setChild(name, change.vision)
                else -> children.getChild(name)?.receiveChange(change)
            }
        }
        change.properties?.let {
            updateProperties(it, Name.EMPTY)
        }
    }
}

public val Vision.children: VisionChildren? get() = (this as? VisionGroup)?.children

/**
 * A full base implementation for a [Vision]
 */
@Serializable
public abstract class AbstractVisionGroup : AbstractVision(), MutableVisionGroup {

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
public class SimpleVisionGroup : AbstractVisionGroup(), MutableVisionContainer<Vision> {
    override fun createGroup(): SimpleVisionGroup = SimpleVisionGroup()

    override fun setChild(name: Name?, child: Vision?) {
        children.setChild(name, child)
    }
}

@VisionBuilder
public inline fun MutableVisionContainer<Vision>.group(
    name: Name? = null,
    builder: SimpleVisionGroup.() -> Unit = {},
): SimpleVisionGroup = SimpleVisionGroup().also { setChild(name, it) }.apply(builder)

/**
 * Define a group with given [name], attach it to this parent and return it.
 */
@VisionBuilder
public inline fun MutableVisionContainer<Vision>.group(
    name: String,
    builder: SimpleVisionGroup.() -> Unit = {},
): SimpleVisionGroup = group(name.parseAsName(), builder)

//fun VisualObject.findStyle(styleName: Name): Meta? {
//    if (this is VisualGroup) {
//        val style = resolveStyle(styleName)
//        if (style != null) return style
//    }
//    return parent?.findStyle(styleName)
//}