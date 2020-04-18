package hep.dataforge.js

import react.RBuilder
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> RBuilder.initState(init: () -> T): ReadWriteProperty<Any?, T> =
    object : ReadWriteProperty<Any?, T> {
        val pair = react.useState(init)
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return pair.first
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            pair.second(value)
        }
    }

