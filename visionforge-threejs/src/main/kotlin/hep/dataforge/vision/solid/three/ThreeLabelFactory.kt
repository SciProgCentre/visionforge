package hep.dataforge.vision.solid.three


import hep.dataforge.context.logger
import hep.dataforge.vision.solid.SolidLabel
import hep.dataforge.vision.solid.three.ThreeMaterials.getMaterial
import info.laht.threekt.core.Object3D
import info.laht.threekt.geometries.TextBufferGeometry
import info.laht.threekt.objects.Mesh
import kotlinext.js.jsObject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        return Mesh(textGeo, getMaterial(obj, true)).apply {
            updatePosition(obj)
            obj.propertyInvalidated.onEach { _ ->
                //TODO
                three.logger.warn{"Label parameter change not implemented"}
            }.launchIn(three.updateScope)
        }
    }
}