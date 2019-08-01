package hep.dataforge.vis.spatial.three

import hep.dataforge.vis.spatial.Proxy3D
import hep.dataforge.vis.spatial.VisualObject3D
import info.laht.threekt.core.Object3D
import info.laht.threekt.objects.Mesh

class ThreeProxyFactory(val three: ThreePlugin) : ThreeFactory<Proxy3D> {
    private val cache = HashMap<VisualObject3D, Mesh>()

    override val type = Proxy3D::class

    override fun invoke(obj: Proxy3D): Object3D {
        val templateMesh = cache.getOrPut(obj.template) {
            three.buildObject3D(obj.template) as Mesh
        }

        //val mesh = Mesh(templateMesh.geometry as BufferGeometry, templateMesh.material)
        val mesh = templateMesh.clone()

        mesh.updatePosition(obj)
        return mesh
    }
}