package hep.dataforge.vision

import hep.dataforge.meta.Meta
import hep.dataforge.meta.boolean
import hep.dataforge.meta.descriptors.ItemDescriptor
import hep.dataforge.meta.descriptors.attributes
import hep.dataforge.meta.get
import hep.dataforge.meta.set

private const val INHERITED_DESCRIPTOR_ATTRIBUTE = "inherited"
private const val STYLE_DESCRIPTOR_ATTRIBUTE = "useStyles"

public var ItemDescriptor.inherited: Boolean
    get() = attributes[INHERITED_DESCRIPTOR_ATTRIBUTE].boolean ?: false
    set(value) = attributes {
        set(INHERITED_DESCRIPTOR_ATTRIBUTE, value)
    }

public var ItemDescriptor.usesStyles: Boolean
    get() = attributes[STYLE_DESCRIPTOR_ATTRIBUTE].boolean ?: true
    set(value) = attributes {
        set(STYLE_DESCRIPTOR_ATTRIBUTE, value)
    }


public val Vision.describedProperties: Meta
    get() = Meta {
        descriptor?.items?.forEach { (key, descriptor) ->
            key put getProperty(key, inherit = descriptor.inherited)
        }
    }

