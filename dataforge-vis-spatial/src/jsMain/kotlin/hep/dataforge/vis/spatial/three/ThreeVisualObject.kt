@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vis.spatial.three

import hep.dataforge.io.serialization.ConfigSerializer
import hep.dataforge.io.toMeta
import hep.dataforge.meta.Config
import hep.dataforge.meta.Meta
import hep.dataforge.vis.common.AbstractVisualObject
import hep.dataforge.vis.spatial.Point3D
import hep.dataforge.vis.spatial.Point3DSerializer
import hep.dataforge.vis.spatial.Visual3DPlugin
import hep.dataforge.vis.spatial.VisualObject3D
import info.laht.threekt.core.Object3D
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * A custom visual object that has its own Three.js renderer
 */
interface ThreeVisualObject : VisualObject3D {
    fun toObject3D(): Object3D
}

@Serializable
class CustomThreeVisualObject(val threeFactory: ThreeFactory<VisualObject3D>) : AbstractVisualObject(),
    ThreeVisualObject {
    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

    override fun toMeta(): Meta = Visual3DPlugin.json.toJson(serializer(), this).toMeta()

    override fun toObject3D(): Object3D = threeFactory(this)

}