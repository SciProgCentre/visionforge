package space.kscience.visionforge.solid.three

import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.cutFirst
import space.kscience.dataforge.names.firstOrNull
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.SolidReference
import space.kscience.visionforge.solid.SolidReference.Companion.REFERENCE_CHILD_PROPERTY_PREFIX
import three.core.Object3D
import three.objects.Mesh
import kotlin.reflect.KClass

public object ThreeReferenceFactory : ThreeFactory<SolidReference> {
    private val cache = HashMap<Solid, Object3D>()

    override val type: KClass<SolidReference> = SolidReference::class

    private fun Object3D.replicate(): Object3D {
        return when {
            isMesh(this) -> Mesh(geometry, material).also {
                it.applyMatrix4(matrix)
            }
            else -> clone(false)
        }.also { obj: Object3D ->
            obj.name = this.name
            children.forEach { child: Object3D ->
                obj.add(child.replicate())
            }
        }
    }

    override fun build(three: ThreePlugin, vision: SolidReference, observe: Boolean): Object3D {
        val template = vision.prototype
        val cachedObject = cache.getOrPut(template) {
            three.buildObject3D(template)
        }

        val object3D: Object3D = cachedObject.replicate()
        object3D.updatePosition(vision)

        if (isMesh(object3D)) {
            //object3D.material = ThreeMaterials.buildMaterial(obj.getProperty(SolidMaterial.MATERIAL_KEY).node!!)
            object3D.applyProperties(vision)
        }

        //TODO apply child properties

        if (observe) {
            vision.onPropertyChange(three.context) { name ->
                if (name.firstOrNull()?.body == REFERENCE_CHILD_PROPERTY_PREFIX) {
                    val childName = name.firstOrNull()?.index?.let(Name::parse)
                        ?: error("Wrong syntax for reference child property: '$name'")
                    val propertyName = name.cutFirst()
                    val referenceChild =
                        vision.children.getChild(childName) ?: error("Reference child with name '$childName' not found")
                    val child = object3D.findChild(childName) ?: error("Object child with name '$childName' not found")
                    child.updateProperty(referenceChild, propertyName)
                } else {
                    object3D.updateProperty(vision, name)
                }
            }
        }


        return object3D
    }
}