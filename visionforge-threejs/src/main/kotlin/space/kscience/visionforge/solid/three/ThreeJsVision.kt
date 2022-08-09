package space.kscience.visionforge.solid.three

import info.laht.threekt.core.Object3D
import space.kscience.visionforge.solid.SolidBase

/**
 * A custom visual object that has its own Three.js renderer
 */
public abstract class ThreeJsVision : SolidBase<ThreeJsVision>() {
    public abstract fun render(three: ThreePlugin): Object3D
}
