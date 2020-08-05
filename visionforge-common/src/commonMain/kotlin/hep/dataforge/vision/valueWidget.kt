package hep.dataforge.vision

import hep.dataforge.meta.Meta
import hep.dataforge.meta.descriptors.ValueDescriptor
import hep.dataforge.meta.descriptors.setAttribute
import hep.dataforge.meta.get
import hep.dataforge.meta.node
import hep.dataforge.meta.string
import hep.dataforge.names.toName

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