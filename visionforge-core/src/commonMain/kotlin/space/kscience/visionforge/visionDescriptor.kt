package space.kscience.visionforge

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.*
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.values.asValue

private const val INHERITED_DESCRIPTOR_ATTRIBUTE = "inherited"
private const val STYLE_DESCRIPTOR_ATTRIBUTE = "useStyles"

public val MetaDescriptor.inherited: Boolean
    get() = attributes[INHERITED_DESCRIPTOR_ATTRIBUTE].boolean ?: false

public var MetaDescriptorBuilder.inherited: Boolean
    get() = attributes[INHERITED_DESCRIPTOR_ATTRIBUTE].boolean ?: false
    set(value) = attributes.set(INHERITED_DESCRIPTOR_ATTRIBUTE, value)


public val MetaDescriptor.usesStyles: Boolean
    get() = attributes[STYLE_DESCRIPTOR_ATTRIBUTE].boolean ?: true

public var MetaDescriptorBuilder.usesStyles: Boolean
    get() = attributes[STYLE_DESCRIPTOR_ATTRIBUTE].boolean ?: true
    set(value) = attributes.set(STYLE_DESCRIPTOR_ATTRIBUTE, value)


public val Vision.describedProperties: Meta
    get() = Meta {
        descriptor?.children?.forEach { (key, descriptor) ->
            this.setMeta(key.asName(), getProperty(key, inherit = descriptor.inherited))
        }
    }

public val MetaDescriptor.widget: Meta
    get() = attributes["widget"] ?: Meta.EMPTY

/**
 * Extension property to access the "widget" key of [ValueDescriptor]
 */
public var MetaDescriptorBuilder.widget: Meta
    get() = attributes["widget"] ?: Meta.EMPTY
    set(value) {
        attributes["widget"] = value
    }

public val MetaDescriptor.widgetType: String?
    get() = attributes["widget.type"].string

/**
 * Extension property to access the "widget.type" key of [ValueDescriptor]
 */
public var MetaDescriptorBuilder.widgetType: String?
    get() = attributes["widget.type"].string
    set(value) {
        attributes["widget.type"] = value?.asValue()
    }

/**
 * If true, this item is hidden in property editor. Default is false
 */
public val MetaDescriptor.hidden: Boolean
    get() = attributes["widget.hide"].boolean ?: false

public fun MetaDescriptorBuilder.hide(): Unit = attributes.set("widget.hide", true)