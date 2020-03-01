package hep.dataforge.vis.spatial.three

import hep.dataforge.js.jsObject
import hep.dataforge.meta.MetaItem
import hep.dataforge.names.Name
import hep.dataforge.vis.spatial.Label3D
import hep.dataforge.vis.spatial.three.ThreeMaterials.getMaterial
import info.laht.threekt.core.Object3D
import info.laht.threekt.geometries.TextBufferGeometry
import info.laht.threekt.objects.Mesh
import kotlin.reflect.KClass

/**
*
 */
object ThreeLabelFactory : ThreeFactory<Label3D> {
    override val type: KClass<in Label3D> get() = Label3D::class

    override fun invoke(obj: Label3D): Object3D {
        val textGeo = TextBufferGeometry( obj.text, jsObject {
            font =  obj.fontFamily
            size = 20
            height = 1
            curveSegments = 1
        } )
        return Mesh(textGeo, getMaterial(obj)).apply {
            updatePosition(obj)
            obj.onPropertyChange(this@ThreeLabelFactory){ _: Name, _: MetaItem<*>?, _: MetaItem<*>? ->
                //TODO
            }
        }
    }
}