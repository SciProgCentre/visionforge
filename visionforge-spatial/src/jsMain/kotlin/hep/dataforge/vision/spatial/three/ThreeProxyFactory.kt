package hep.dataforge.vision.spatial.three

import hep.dataforge.names.toName
import hep.dataforge.vision.spatial.Proxy
import hep.dataforge.vision.spatial.Proxy.Companion.PROXY_CHILD_PROPERTY_PREFIX
import hep.dataforge.vision.spatial.VisualObject3D
import info.laht.threekt.core.Object3D

class ThreeProxyFactory(val three: ThreePlugin) : ThreeFactory<Proxy> {
    private val cache = HashMap<VisualObject3D, Object3D>()

    override val type = Proxy::class

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