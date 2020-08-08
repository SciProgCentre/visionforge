package hep.dataforge.vision.solid.three

import hep.dataforge.js.jsObject
import hep.dataforge.vision.solid.SolidLabel
import hep.dataforge.vision.solid.three.ThreeMaterials.getMaterial
import info.laht.threekt.core.Object3D
import info.laht.threekt.geometries.TextBufferGeometry
import info.laht.threekt.objects.Mesh
import kotlin.reflect.KClass

/**
 *
 */
object ThreeLabelFactory : ThreeFactory<SolidLabel> {
    override val type: KClass<in SolidLabel> get() = SolidLabel::class

    override fun invoke(obj: SolidLabel): Object3D {
        val textGeo = TextBufferGeometry(obj.text, jsObject {
            font = obj.fontFamily
            size = 20
            height = 1
            curveSegments = 1
        })
        return Mesh(textGeo, getMaterial(obj)).apply {
            updatePosition(obj)
            obj.onPropertyChange(this@ThreeLabelFactory) { _ ->
                //TODO
            }
        }
    }
}