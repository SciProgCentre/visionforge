package hep.dataforge.vision.layout

import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.vision.Vision

public interface Output<in V : Vision> {
    public fun render(vision: V)
}

public interface Page<in V : Vision> {
    public fun output(name: Name, meta: Meta = Meta.EMPTY): Output<V>?
}

public fun <V : Vision> Page<V>.render(name: Name, vision: V): Unit =
    output(name)?.render(vision) ?: error("Could not resolve renderer for name $name")