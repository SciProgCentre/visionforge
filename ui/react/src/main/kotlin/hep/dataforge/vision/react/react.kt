package hep.dataforge.vision.react

import react.*

public class RFBuilder : RBuilder()

/**
 * Get functional component from [func]
 */
public inline fun <P : RProps> component(
    crossinline func: RFBuilder.(props: P) -> Unit,
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
//
//public fun <T> RFBuilder.memoize(vararg deps: dynamic, builder: () -> T): T = useMemo(builder, deps)

