package hep.dataforge.vision.solid

import hep.dataforge.names.*
import hep.dataforge.vision.Vision
import javafx.scene.Group
import javafx.scene.Node
import kotlin.reflect.KClass

class FXReferenceFactory(val plugin: FX3DPlugin) : FX3DFactory<SolidReference> {
    override val type: KClass<in SolidReference> get() = SolidReference::class

    override fun invoke(obj: SolidReference, binding: VisualObjectFXBinding): Node {
        val prototype = obj.prototype
        val node = plugin.buildNode(prototype)

        obj.onPropertyChange(this) { name->
            if (name.firstOrNull()?.body == SolidReference.REFERENCE_CHILD_PROPERTY_PREFIX) {
                val childName = name.firstOrNull()?.index?.toName() ?: error("Wrong syntax for reference child property: '$name'")
                val propertyName = name.cutFirst()
                val referenceChild = obj[childName] ?: error("Reference child with name '$childName' not found")
                val child = node.findChild(childName) ?: error("Object child with name '$childName' not found")
                child.updateProperty(referenceChild, propertyName)
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
            ?.find { it.properties["name"] as String == name.firstOrNull()?.toString() }
            ?.findChild(name.cutFirst())
    }
}

private fun Node.updateProperty(obj: Vision, propertyName: Name) {
//    if (propertyName.startsWith(Material3D.MATERIAL_KEY)) {
//        (this as? Shape3D)?.let { it.material = obj.getProperty(Material3D.MATERIAL_KEY).material() }
//    }
}