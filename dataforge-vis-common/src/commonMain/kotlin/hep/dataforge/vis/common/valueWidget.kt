package hep.dataforge.vis.common

import hep.dataforge.descriptors.ValueDescriptor
import hep.dataforge.meta.*

var ValueDescriptor.widget: Meta
    get() = this.config["widget"].node?: EmptyMeta
    set(value) {
        this.config["widget"] = value
    }

var ValueDescriptor.widgetType: String?
    get() = this["widget.type"].string
    set(value) {
        this.config["widget.type"] = value
    }