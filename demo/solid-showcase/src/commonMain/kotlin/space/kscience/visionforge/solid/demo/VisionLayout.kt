package space.kscience.visionforge.solid.demo

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision

public interface VisionLayout<in V: Vision> {
    public fun render(name: Name, vision: V, meta: Meta = Meta.EMPTY)
}