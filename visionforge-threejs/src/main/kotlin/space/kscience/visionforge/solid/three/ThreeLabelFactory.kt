package space.kscience.visionforge.solid.three


import info.laht.threekt.core.Object3D
import info.laht.threekt.geometries.TextBufferGeometry
import info.laht.threekt.objects.Mesh
import kotlinext.js.jsObject
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.context.warn
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.SolidLabel
import kotlin.reflect.KClass

/**
 *
 */
public object ThreeLabelFactory : ThreeFactory<SolidLabel> {
    override val type: KClass<in SolidLabel> get() = SolidLabel::class

    override fun invoke(three: ThreePlugin, obj: SolidLabel): Object3D {
        val textGeo = TextBufferGeometry(obj.text, jsObject {
            font = obj.fontFamily
            size = 20
            height = 1
            curveSegments = 1
        })
        return Mesh(textGeo, ThreeMaterials.DEFAULT).apply {
            updateMaterial(obj)
            updatePosition(obj)
            obj.onPropertyChange { _ ->
                //TODO
                three.logger.warn { "Label parameter change not implemented" }
            }
        }
    }
}