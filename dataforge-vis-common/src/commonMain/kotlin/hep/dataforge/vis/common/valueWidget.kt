package hep.dataforge.vis.common

import hep.dataforge.descriptors.ValueDescriptor
import hep.dataforge.meta.*

/**
 * Extension property to access the "widget" key of [ValueDescriptor]
 */
var ValueDescriptor.widget: Meta
    get() = this.config["widget"].node?: EmptyMeta
    set(value) {
        this.config["widget"] = value
    }

/**
 * Extension property to access the "widget.type" key of [ValueDescriptor]
 */
var ValueDescriptor.widgetType: String?
    get() = this["widget.type"].string
    set(value) {
        this.config["widget.type"] = value
    }