package hep.dataforge.vis.spatial.gdml

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.color
import hep.dataforge.vis.spatial.*
import scientifik.gdml.*
import kotlin.math.cos
import kotlin.math.sin


private fun VisualObject.withPosition(
    pos: GDMLPosition? = null,
    rotation: GDMLRotation? = null,
    scale: GDMLScale? = null
): VisualObject =
    apply {
        //        if( this is VisualObject3D){
//            pos?.let {
//                x = pos.x
//                y = pos.y
//                z = pos.z
//            }
//            rotation?.let {
//                rotationX = rotation.x
//                rotationY = rotation.y
//                rotationZ = rotation.z
//            }
//        } else {
        pos?.let {
            x = pos.x
            y = pos.y
            z = pos.z
        }
        rotation?.let {
            rotationX = rotation.x
            rotationY = rotation.y
            rotationZ = rotation.z
        }
        //}
        scale?.let {
            scaleX = scale.x
            scaleY = scale.y
            scaleZ = scale.z
        }
        //TODO convert units if needed
    }


private fun VisualGroup.addSolid(
    root: GDML,
    solid: GDMLSolid,
    name: String? = null,
    block: VisualObject.() -> Unit = {}
): VisualObject {
    return when (solid) {
        is GDMLBox -> box(solid.x, solid.y, solid.z, name)
        is GDMLTube -> cylinder(solid.rmax, solid.z, name) {
            startAngle = solid.startphi
            angle = solid.deltaphi
        }
        is GDMLXtru -> extrude(name) {
            shape {
                solid.vertices.forEach {
                    point(it.x, it.y)
                }
            }
            solid.sections.sortedBy { it.zOrder }.forEach { section ->
                layer(section.zPosition, section.xOffset, section.yOffset, section.scalingFactor)
            }
        }
        is GDMLScaledSolid -> {
            //Add solid with modified scale
            val innerSolid = solid.solidref.resolve(root)
                ?: error("Solid with tag ${solid.solidref.ref} for scaled solid ${solid.name} not defined")

            addSolid(root, innerSolid) {
                block()
                scaleX = scaleX.toDouble() * solid.scale.x.toDouble()
                scaleY = scaleY.toDouble() * solid.scale.y.toDouble()
                scaleZ = scaleZ.toDouble() * solid.scale.z.toDouble()
            }
        }
        is GDMLSphere -> sphere(solid.rmax, solid.deltaphi, solid.deltatheta, name) {
            phiStart = solid.startphi.toDouble()
            thetaStart = solid.starttheta.toDouble()
        }
        is GDMLOrb -> sphere(solid.r, name = name)
        is GDMLPolyhedra -> extrude(name) {
            //getting the radius of first
            require(solid.planes.size > 1) { "The polyhedron geometry requires at least two planes" }
            val baseRadius = solid.planes.first().rmax.toDouble()
            shape {
                (0..solid.numsides).forEach {
                    val phi = solid.deltaphi.toDouble() / solid.numsides * it + solid.startphi.toDouble()
                    baseRadius * cos(phi) to baseRadius * sin(phi)
                }
            }
            solid.planes.forEach { plane ->
                //scaling all radii relative to first layer radius
                layer(plane.z, scale = plane.rmax.toDouble() / baseRadius)
            }
        }
        is GDMLBoolSolid -> {
            val first = solid.first.resolve(root) ?: error("")
            val second = solid.second.resolve(root) ?: error("")
            val type: CompositeType = when (solid) {
                is GDMLUnion -> CompositeType.UNION
                is GDMLSubtraction -> CompositeType.SUBTRACT
                is GDMLIntersection -> CompositeType.INTERSECT
            }

            return composite(type, name) {
                addSolid(root, first) {
                    withPosition(solid.resolveFirstPosition(root), solid.resolveFirstRotation(root), null)
                }
                addSolid(root, second)
                withPosition(solid.resolvePosition(root), solid.resolveRotation(root), null)
            }
        }
    }.apply(block)
}

private fun VisualGroup.addVolume(
    root: GDML,
    group: GDMLGroup,
    position: GDMLPosition? = null,
    rotation: GDMLRotation? = null,
    scale: GDMLScale? = null,
    resolveColor: GDMLMaterial.() -> Meta
) {

    group(group.name) {
        withPosition(position, rotation, scale)

        if (group is GDMLVolume) {
            val solid = group.solidref.resolve(root)
                ?: error("Solid with tag ${group.solidref.ref} for volume ${group.name} not defined")
            val material = group.materialref.resolve(root)
                ?: error("Material with tag ${group.materialref.ref} for volume ${group.name} not defined")

            addSolid(root, solid, solid.name) {
                color(material.resolveColor())
            }
            //TODO render placements
        }

        group.physVolumes.forEach { physVolume ->
            val volume: GDMLGroup = physVolume.volumeref.resolve(root)
                ?: error("Volume with ref ${physVolume.volumeref.ref} could not be resolved")

            addVolume(
                root,
                volume,
                physVolume.resolvePosition(root),
                physVolume.resolveRotation(root),
                physVolume.resolveScale(root),
                resolveColor
            )
        }
    }

}


fun GDML.toVisual(): VisualGroup {
    //TODO add materials cache
    fun GDMLMaterial.color(): Meta = EmptyMeta
    return VisualGroup().also { it.addVolume(this, world) { color() } }
}