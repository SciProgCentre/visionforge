@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vision.spatial.three

import hep.dataforge.meta.Config
import hep.dataforge.vision.AbstractVision
import hep.dataforge.vision.spatial.Point3D
import hep.dataforge.vision.spatial.Point3DSerializer
import hep.dataforge.vision.spatial.Vision3D
import info.laht.threekt.core.Object3D
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * A custom visual object that has its own Three.js renderer
 */
interface ThreeVision : Vision3D {
    fun toObject3D(): Object3D
}

@Serializable
class CustomThreeVision(val threeFactory: ThreeFactory<Vision3D>) : AbstractVision(),
    ThreeVision {
    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    override var ownProperties: Config? = null

    override fun toObject3D(): Object3D = threeFactory(this)

}