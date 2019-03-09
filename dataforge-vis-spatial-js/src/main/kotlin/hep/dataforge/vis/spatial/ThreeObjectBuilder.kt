package hep.dataforge.vis.spatial

import hep.dataforge.vis.DisplayObject
import hep.dataforge.vis.get
import hep.dataforge.vis.onChange
import hep.dataforge.vis.spatial.three.ConvexGeometry
import hep.dataforge.vis.spatial.three.EdgesGeometry
import hep.dataforge.vis.spatial.three.euler
import info.laht.threekt.core.Object3D
import info.laht.threekt.geometries.BoxBufferGeometry
import info.laht.threekt.math.Vector3
import info.laht.threekt.objects.LineSegments
import info.laht.threekt.objects.Mesh

/**
 * Builder and updater for three.js object
 */
interface ThreeObjectBuilder<in T : DisplayObject> {

    operator fun invoke(obj: T): Object3D

    companion object {
        /**
         * Update position, rotation and visibility
         */
        internal fun updatePosition(obj: DisplayObject, target: Object3D) {
            target.apply {
                position.set(obj.x, obj.y, obj.z)
                setRotationFromEuler(obj.euler)
                scale.set(obj.scaleX, obj.scaleY, obj.scaleZ)
                visible = obj.visible
            }
        }
    }
}

abstract class GenericThreeBuilder<in T : DisplayObject, R : Object3D> : ThreeObjectBuilder<T> {
    /**
     * Build an object
     */
    abstract fun build(obj: T): R

    /**
     * Update an object
     */
    abstract fun update(obj: T, target: R)

    override fun invoke(obj: T): R {
        val target = build(obj)
        ThreeObjectBuilder.updatePosition(obj, target)
        obj.onChange(this) { _, _, _ ->
            ThreeObjectBuilder.updatePosition(obj, target)
            update(obj, target)
        }
        return target
    }
}

object ThreeBoxBuilder : GenericThreeBuilder<Box, Mesh>() {
    override fun build(obj: Box): Mesh {
        val geometry = BoxBufferGeometry(obj.xSize, obj.ySize, obj.zSize)
        return Mesh(geometry, obj["color"].material()).also { mesh ->
            mesh.add(LineSegments(EdgesGeometry(geometry), Materials.DEFAULT))
        }
    }

    override fun update(obj: Box, target: Mesh) {
        target.geometry = BoxBufferGeometry(obj.xSize, obj.ySize, obj.zSize)
        target.material = obj["color"].material()
    }
}

fun Point3D.asVector(): Vector3 = this.asDynamic() as Vector3

object ThreeConvexBuilder: GenericThreeBuilder<Convex,Mesh>(){
    override fun build(obj: Convex): Mesh {
        val geometry = ConvexGeometry(obj.points.map { it.asVector() }.toTypedArray())
        return Mesh(geometry, obj["color"].material()).also { mesh ->
            mesh.add(LineSegments(EdgesGeometry(geometry), Materials.DEFAULT))
        }
    }

    override fun update(obj: Convex, target: Mesh) {
        target.geometry = ConvexGeometry(obj.points.map { it.asVector() }.toTypedArray())
        target.material = obj["color"].material()
    }
}