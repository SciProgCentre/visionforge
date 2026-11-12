package space.kscience.visionforge.solid.three

import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.cutFirst
import space.kscience.dataforge.names.firstOrNull
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.SolidReference
import space.kscience.visionforge.solid.SolidReference.Companion.REFERENCE_CHILD_PROPERTY_PREFIX
import space.kscience.visionforge.solid.getVision
import three.core.Object3D
import three.objects.Mesh
import kotlin.reflect.KClass

private fun Object3D.lockMaterial(){
    if(this is Mesh){
        material.cached = true
    }
    children.forEach {
        it.lockMaterial()
    }
}

public object ThreeReferenceFactory : ThreeFactory<SolidReference> {
    private val cache = HashMap<Solid, Object3D>()

    override val type: KClass<SolidReference> = SolidReference::class

//    private fun Object3D.replicate(): Object3D = when {
//        isMesh(this) -> Mesh(geometry, material).also {
//            //clone geometry
//            it.material.cached = true
//            it.applyMatrix4(matrix)
//        }
//
//        else -> clone(false)
//    }.also { obj: Object3D ->
//        obj.name = this.name
//        children.forEach { child: Object3D ->
//            obj.add(child.replicate())
//        }
//    }


    override suspend fun build(three: ThreePlugin, vision: SolidReference, observe: Boolean): Object3D {
        val template = vision.prototype
        val cachedObject = cache.getOrPut(template) {
            three.buildObject3D(template).apply {
                lockMaterial()
            }
        }

        val object3D: Object3D = cachedObject.clone(true)
        object3D.updatePosition(vision)

        if (object3D.isMesh()) {
            object3D.applyProperties(vision)
        }

        //TODO apply child properties

        if (observe) {
            vision.onPropertyChange(three.context) { name, _ ->
                if (name.firstOrNull()?.body == REFERENCE_CHILD_PROPERTY_PREFIX) {
                    val childName = name.firstOrNull()?.index?.let(Name::parse)
                        ?: error("Wrong syntax for reference child property: '$name'")
                    val propertyName = name.cutFirst()
                    val referenceChild = vision.getVision(childName)
                        ?: error("Reference child with name '$childName' not found")
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