package hep.dataforge.vis.spatial.three

import hep.dataforge.vis.spatial.Proxy
import hep.dataforge.vis.spatial.VisualObject3D
import info.laht.threekt.core.Object3D

class ThreeProxyFactory(val three: ThreePlugin) : ThreeFactory<Proxy> {
    private val cache = HashMap<VisualObject3D, Object3D>()

    override val type = Proxy::class

    override fun invoke(obj: Proxy): Object3D {
        val template = obj.template
        val cachedObject = cache.getOrPut(template) {
            three.buildObject3D(template)
        }

        //val mesh = Mesh(templateMesh.geometry as BufferGeometry, templateMesh.material)
        val mesh = cachedObject.clone()

        mesh.updatePosition(obj)
        return mesh
    }
}