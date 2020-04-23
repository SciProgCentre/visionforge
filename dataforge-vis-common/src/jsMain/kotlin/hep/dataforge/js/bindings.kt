package hep.dataforge.js

import hep.dataforge.properties.Property
import org.w3c.dom.HTMLInputElement

fun HTMLInputElement.bindValue(property: Property<String>) {
    if (this.onchange != null) error("Input element already bound")
    this.onchange = {
        property.value = this.value
        Unit
    }
    property.onChange(this) {
        if (value != it) {
            value = it
        }
    }
}

fun HTMLInputElement.bindChecked(property: Property<Boolean>) {
    if (this.onchange != null) error("Input element already bound")
    this.onchange = {
        property.value = this.checked
        Unit
    }
    property.onChange(this) {
        if (checked != it) {
            checked = it
        }
    }
}