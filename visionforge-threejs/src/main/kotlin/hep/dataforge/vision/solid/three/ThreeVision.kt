package hep.dataforge.vision.solid.three

import hep.dataforge.vision.solid.SolidBase
import info.laht.threekt.core.Object3D

/**
 * A custom visual object that has its own Three.js renderer
 */
public abstract class ThreeVision : SolidBase() {
    public abstract fun render(): Object3D
}
