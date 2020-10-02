package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.ValueDescriptor
import hep.dataforge.meta.descriptors.attributes

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
        attributes{
            set("widget.type", value)
        }
    }