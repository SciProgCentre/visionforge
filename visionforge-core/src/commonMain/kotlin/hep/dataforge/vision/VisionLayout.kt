package hep.dataforge.vision

import hep.dataforge.meta.Meta
import hep.dataforge.names.Name

public interface VisionLayout<in V: Vision> {
    public fun render(name: Name, vision: V, meta: Meta = Meta.EMPTY)
}