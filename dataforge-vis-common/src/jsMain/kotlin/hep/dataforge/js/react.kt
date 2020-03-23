package hep.dataforge.js

import react.RComponent
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> RComponent<*, *>.initState(init: () -> T): ReadWriteProperty<RComponent<*, *>, T> =
    object : ReadWriteProperty<RComponent<*, *>, T> {
        val pair = react.useState(init)
        override fun getValue(thisRef: RComponent<*, *>, property: KProperty<*>): T {
            return pair.first
        }

        override fun setValue(thisRef: RComponent<*, *>, property: KProperty<*>, value: T) {
            pair.second(value)
        }
    }