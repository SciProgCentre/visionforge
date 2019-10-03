package hep.dataforge.vis.spatial.three

import hep.dataforge.names.toName
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.spatial.Proxy
import hep.dataforge.vis.spatial.Proxy.Companion.PROXY_CHILD_PROPERTY_PREFIX
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
        val template = obj.prototype
        val cachedObject = cache.getOrPut(template) {
            three.buildObject3D(template)
        }

        //val mesh = Mesh(templateMesh.geometry as BufferGeometry, templateMesh.material)
        val object3D = cachedObject.clone()
        object3D.updatePosition(obj)

        obj.onPropertyChange(this) { name, _, _ ->
            if (name.first()?.body == PROXY_CHILD_PROPERTY_PREFIX) {
                val childName = name.first()?.index?.toName() ?: error("Wrong syntax for proxy child property: '$name'")
                val propertyName = name.cutFirst()
                val proxyChild = obj[childName] ?: error("Proxy child with name '$childName' not found")
                val child = object3D.findChild(childName)?: error("Object child with name '$childName' not found")
                child.updateProperty(proxyChild, propertyName)
            } else {
                object3D.updateProperty(obj, name)
            }
        }

        return object3D
    }
}