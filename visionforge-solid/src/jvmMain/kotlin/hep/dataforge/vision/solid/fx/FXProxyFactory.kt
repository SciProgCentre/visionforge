package hep.dataforge.vision.solid.fx

import hep.dataforge.names.Name
import hep.dataforge.names.isEmpty
import hep.dataforge.names.toName
import hep.dataforge.vision.Vision
import hep.dataforge.vision.solid.Proxy
import javafx.scene.Group
import javafx.scene.Node
import kotlin.reflect.KClass

class FXProxyFactory(val plugin: FX3DPlugin) : FX3DFactory<Proxy> {
    override val type: KClass<in Proxy> get() = Proxy::class

    override fun invoke(obj: Proxy, binding: VisualObjectFXBinding): Node {
        val prototype = obj.prototype
        val node = plugin.buildNode(prototype)

        obj.onPropertyChange(this) { name->
            if (name.first()?.body == Proxy.PROXY_CHILD_PROPERTY_PREFIX) {
                val childName = name.first()?.index?.toName() ?: error("Wrong syntax for proxy child property: '$name'")
                val propertyName = name.cutFirst()
                val proxyChild = obj[childName] ?: error("Proxy child with name '$childName' not found")
                val child = node.findChild(childName) ?: error("Object child with name '$childName' not found")
                child.updateProperty(proxyChild, propertyName)
            }
        }
        return node
    }
}

private fun Node.findChild(name: Name): Node? {
    return if (name.isEmpty()) {
        this
    } else {
        (this as? Group)
            ?.children
            ?.find { it.properties["name"] as String == name.first()?.toString() }
            ?.findChild(name.cutFirst())
    }
}

private fun Node.updateProperty(obj: Vision, propertyName: Name) {
//    if (propertyName.startsWith(Material3D.MATERIAL_KEY)) {
//        (this as? Shape3D)?.let { it.material = obj.getProperty(Material3D.MATERIAL_KEY).material() }
//    }
}