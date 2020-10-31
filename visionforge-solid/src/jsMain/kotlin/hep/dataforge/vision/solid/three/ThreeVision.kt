package hep.dataforge.vision.solid.three

import hep.dataforge.vision.solid.AbstractSolid
import hep.dataforge.vision.solid.Solid
import info.laht.threekt.core.Object3D
import kotlinx.serialization.Serializable

/**
 * A custom visual object that has its own Three.js renderer
 */
public interface ThreeVision : Solid {
    public fun toObject3D(): Object3D
}

@Serializable
public class CustomThreeVision(public val threeFactory: ThreeFactory<Solid>) : AbstractSolid(), ThreeVision {
    override fun toObject3D(): Object3D = threeFactory(this)
}