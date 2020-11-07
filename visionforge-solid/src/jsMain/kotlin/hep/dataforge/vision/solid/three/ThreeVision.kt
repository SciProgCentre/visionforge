package hep.dataforge.vision.solid.three

import hep.dataforge.vision.solid.BasicSolid
import hep.dataforge.vision.solid.Solid
import info.laht.threekt.core.Object3D
import kotlinx.serialization.Serializable

/**
 * A custom visual object that has its own Three.js renderer
 */
public abstract class ThreeVision : BasicSolid() {
    public abstract fun render(): Object3D
}
