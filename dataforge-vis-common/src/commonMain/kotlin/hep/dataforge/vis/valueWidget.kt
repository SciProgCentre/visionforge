package hep.dataforge.vis

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.ValueDescriptor
import hep.dataforge.values.asValue

/**
 * Extension property to access the "widget" key of [ValueDescriptor]
 */
var ValueDescriptor.widget: Meta
    get() = getProperty("widget").node ?: Meta.EMPTY
    set(value) {
        setProperty("widget", value)
    }

/**
 * Extension property to access the "widget.type" key of [ValueDescriptor]
 */
var ValueDescriptor.widgetType: String?
    get() = getProperty("widget.type").string
    set(value) {
        setProperty("widget.type", value?.asValue())
    }