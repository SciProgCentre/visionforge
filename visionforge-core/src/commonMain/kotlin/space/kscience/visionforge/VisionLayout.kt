package space.kscience.visionforge

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

public interface VisionLayout<in V: Vision> {
    public fun render(name: Name, vision: V, meta: Meta = Meta.EMPTY)
}