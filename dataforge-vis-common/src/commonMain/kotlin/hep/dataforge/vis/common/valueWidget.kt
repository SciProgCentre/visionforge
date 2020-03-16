package hep.dataforge.vis.common

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.ValueDescriptor
import hep.dataforge.meta.scheme.getProperty

/**
 * Extension property to access the "widget" key of [ValueDescriptor]
 */
var ValueDescriptor.widget: Meta
    get() = this.config["widget"].node?: EmptyMeta
    set(value) {
        config["widget"] = value
    }

/**
 * Extension property to access the "widget.type" key of [ValueDescriptor]
 */
var ValueDescriptor.widgetType: String?
    get() = getProperty("widget.type").string
    set(value) {
        config["widget.type"] = value
    }