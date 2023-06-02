package space.kscience.visionforge.solid.three

import space.kscience.visionforge.solid.SolidBase
import three.core.Object3D

/**
 * A custom visual object that has its own Three.js renderer
 */
public abstract class ThreeJsVision : SolidBase<ThreeJsVision>() {
    public abstract fun render(three: ThreePlugin): Object3D
}
