package hep.dataforge.vis.spatial.three

import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.spatial.Proxy
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.material
import hep.dataforge.vis.spatial.visible
import info.laht.threekt.core.Object3D
import info.laht.threekt.objects.Mesh

class ThreeProxyFactory(val three: ThreePlugin) : ThreeFactory<Proxy> {
    private val cache = HashMap<VisualObject3D, Object3D>()

    override val type = Proxy::class

    private fun Mesh.updateProperties(obj: VisualObject?) {
        material = obj?.material.jsMaterial()
        visible = obj?.visible ?: true
    }

    override fun invoke(obj: Proxy): Object3D {
        val template = obj.template
        val cachedObject = cache.getOrPut(template) {
            three.buildObject3D(template)
        }

        //val mesh = Mesh(templateMesh.geometry as BufferGeometry, templateMesh.material)
        val object3D = cachedObject.clone()
        object3D.updatePosition(obj)

        obj.onChildrenChange(object3D) { name, propertyHolder ->
            (object3D.findChild(name) as? Mesh)?.updateProperties(propertyHolder)
        }

        return object3D
    }
}