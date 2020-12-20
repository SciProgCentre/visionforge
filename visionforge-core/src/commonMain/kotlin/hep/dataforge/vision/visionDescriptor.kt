package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.ItemDescriptor
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.meta.descriptors.ValueDescriptor
import hep.dataforge.meta.descriptors.attributes
import hep.dataforge.names.Name
import hep.dataforge.values.ValueType
import hep.dataforge.values.asValue

private const val INHERITED_DESCRIPTOR_ATTRIBUTE = "inherited"
private const val STYLE_DESCRIPTOR_ATTRIBUTE = "useStyles"

public var ItemDescriptor.inherited: Boolean
    get() = attributes[INHERITED_DESCRIPTOR_ATTRIBUTE].boolean ?: false
    set(value) = attributes {
        set(INHERITED_DESCRIPTOR_ATTRIBUTE, value)
    }

public var ItemDescriptor.usesStyles: Boolean
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

/**
 * Extension property to access the "widget" key of [ValueDescriptor]
 */
public var ValueDescriptor.widget: Meta
    get() = attributes["widget"].node ?: Meta.EMPTY
    set(value) {
        attributes {
            set("widget", value)
        }
    }

/**
 * Extension property to access the "widget.type" key of [ValueDescriptor]
 */
public var ValueDescriptor.widgetType: String?
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

public fun ItemDescriptor.hide(): Unit = attributes {
    set("widget.hide", true)
}


public inline fun <reified E : Enum<E>> NodeDescriptor.enum(
    key: Name,
    default: E?,
    crossinline modifier: ValueDescriptor.() -> Unit = {},
): Unit = value(key) {
    type(ValueType.STRING)
    default?.let {
        default(default)
    }
    allowedValues = enumValues<E>().map { it.asValue() }
    modifier()
}