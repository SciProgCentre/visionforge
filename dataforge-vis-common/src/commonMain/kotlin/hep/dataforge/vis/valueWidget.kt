package hep.dataforge.vis

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.ValueDescriptor
import hep.dataforge.meta.descriptors.attributes
import hep.dataforge.meta.descriptors.setAttribute
import hep.dataforge.names.toName
import hep.dataforge.values.asValue

/**
 * Extension property to access the "widget" key of [ValueDescriptor]
 */
var ValueDescriptor.widget: Meta
    get() = attributes["widget"].node ?: Meta.EMPTY
    set(value) {
        setAttribute("widget".toName(), value)
    }

/**
 * Extension property to access the "widget.type" key of [ValueDescriptor]
 */
var ValueDescriptor.widgetType: String?
    get() = attributes["widget.type"].string
    set(value) {
        setAttribute("widget.type".toName(), value)
    }