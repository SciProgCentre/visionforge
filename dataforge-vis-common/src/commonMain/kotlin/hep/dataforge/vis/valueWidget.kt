package hep.dataforge.vis

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.meta.descriptors.ValueDescriptor
import hep.dataforge.meta.node
import hep.dataforge.meta.scheme.getProperty
import hep.dataforge.meta.scheme.setProperty
import hep.dataforge.meta.string
import hep.dataforge.values.asValue

/**
 * Extension property to access the "widget" key of [ValueDescriptor]
 */
var ValueDescriptor.widget: Meta
    get() = getProperty("widget").node ?: EmptyMeta
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