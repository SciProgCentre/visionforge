package space.kscience.visionforge

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.values.ValueType
import space.kscience.dataforge.values.asValue

private const val INHERITED_DESCRIPTOR_ATTRIBUTE = "inherited"
private const val STYLE_DESCRIPTOR_ATTRIBUTE = "useStyles"

public val ItemDescriptor.inherited: Boolean
    get() = attributes[INHERITED_DESCRIPTOR_ATTRIBUTE].boolean ?: false

public var ItemDescriptorBuilder.inherited: Boolean
    get() = attributes[INHERITED_DESCRIPTOR_ATTRIBUTE].boolean ?: false
    set(value) = attributes {
        set(INHERITED_DESCRIPTOR_ATTRIBUTE, value)
    }

public val ItemDescriptor.usesStyles: Boolean
    get() = attributes[STYLE_DESCRIPTOR_ATTRIBUTE].boolean ?: true

public var ItemDescriptorBuilder.usesStyles: Boolean
    get() = attributes[STYLE_DESCRIPTOR_ATTRIBUTE].boolean ?: true
    set(value) = attributes {
        set(STYLE_DESCRIPTOR_ATTRIBUTE, value)
    }


public val Vision.describedProperties: Meta
    get() = Meta {
        descriptor?.items?.forEach { (key, descriptor) ->
            key put getProperty(key, inherit = descriptor.inherited)
        }
    }

public val ValueDescriptor.widget: Meta
    get() = attributes["widget"].node ?: Meta.EMPTY

/**
 * Extension property to access the "widget" key of [ValueDescriptor]
 */
public var ValueDescriptorBuilder.widget: Meta
    get() = attributes["widget"].node ?: Meta.EMPTY
    set(value) {
        attributes {
            set("widget", value)
        }
    }

public val ValueDescriptor.widgetType: String?
    get() = attributes["widget.type"].string

/**
 * Extension property to access the "widget.type" key of [ValueDescriptor]
 */
public var ValueDescriptorBuilder.widgetType: String?
    get() = attributes["widget.type"].string
    set(value) {
        attributes {
            set("widget.type", value)
        }
    }

/**
 * If true, this item is hidden in property editor. Default is false
 */
public val ItemDescriptor.hidden: Boolean
    get() = attributes["widget.hide"].boolean ?: false

public fun ItemDescriptorBuilder.hide(): Unit = attributes {
    set("widget.hide", true)
}


public inline fun <reified E : Enum<E>> NodeDescriptorBuilder.enum(
    key: Name,
    default: E?,
    crossinline modifier: ValueDescriptorBuilder.() -> Unit = {},
): Unit = value(key) {
    type(ValueType.STRING)
    default?.let {
        default(default)
    }
    allowedValues = enumValues<E>().map { it.asValue() }
    modifier()
}