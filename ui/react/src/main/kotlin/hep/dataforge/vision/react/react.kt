package hep.dataforge.vision.react

import react.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class RFBuilder : RBuilder()

/**
 * Get functional component from [func]
 */
fun <P : RProps> component(
    func: RFBuilder.(props: P) -> Unit
): FunctionalComponent<P> {
    return { props: P ->
        val nodes = RFBuilder().apply { func(props) }.childList
        when (nodes.size) {
            0 -> null
            1 -> nodes.first()
            else -> createElement(Fragment, kotlinext.js.js {}, *nodes.toTypedArray())
        }
    }
}

fun <T> RFBuilder.state(init: () -> T): ReadWriteProperty<Any?, T> =
    object : ReadWriteProperty<Any?, T> {
        val pair = react.useState(init)
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return pair.first
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            pair.second(value)
        }
    }

fun <T> RFBuilder.memoize(vararg deps: dynamic, builder: () -> T): T = useMemo(builder, deps)

