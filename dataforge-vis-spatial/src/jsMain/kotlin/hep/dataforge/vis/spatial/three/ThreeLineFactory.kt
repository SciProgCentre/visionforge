package hep.dataforge.vis.spatial.three

import hep.dataforge.meta.node
import hep.dataforge.vis.spatial.PolyLine
import hep.dataforge.vis.spatial.layer
import info.laht.threekt.core.Geometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.objects.LineSegments
import kotlin.reflect.KClass

object ThreeLineFactory : ThreeFactory<PolyLine> {
    override val type: KClass<PolyLine> get() = PolyLine::class

    override fun invoke(obj: PolyLine): Object3D {
        val geometry = Geometry().apply {
            vertices = obj.points.toTypedArray()
        }

        val material =
            ThreeMaterials.getLineMaterial(obj.getProperty(MeshThreeFactory.EDGES_MATERIAL_KEY).node)
        return LineSegments(geometry, material).apply {

            updatePosition(obj)
            layers.enable(obj.layer)

            //add listener to object properties
            obj.onPropertyChange(this) { propertyName, _, _ ->
                updateProperty(obj, propertyName)
            }
        }
    }

}