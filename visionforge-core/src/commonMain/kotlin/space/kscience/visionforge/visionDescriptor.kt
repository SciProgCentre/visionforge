package space.kscience.visionforge

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.*
import space.kscience.dataforge.meta.set

private const val INHERITED_DESCRIPTOR_ATTRIBUTE = "inherited"
private const val STYLE_DESCRIPTOR_ATTRIBUTE = "useStyles"

public val MetaDescriptor.inherited: Boolean?
    get() = attributes[INHERITED_DESCRIPTOR_ATTRIBUTE].boolean

public var MetaDescriptorBuilder.inherited: Boolean?
    get() = attributes[INHERITED_DESCRIPTOR_ATTRIBUTE].boolean
    set(value) = attributes.set(INHERITED_DESCRIPTOR_ATTRIBUTE, value?.asValue())


public val MetaDescriptor.usesStyles: Boolean?
    get() = attributes[STYLE_DESCRIPTOR_ATTRIBUTE].boolean

public var MetaDescriptorBuilder.usesStyles: Boolean?
    get() = attributes[STYLE_DESCRIPTOR_ATTRIBUTE].boolean
    set(value) = attributes.set(STYLE_DESCRIPTOR_ATTRIBUTE, value?.asValue())

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
 * Extension property to access the "widget.type" key of [MetaDescriptorBuilder]
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