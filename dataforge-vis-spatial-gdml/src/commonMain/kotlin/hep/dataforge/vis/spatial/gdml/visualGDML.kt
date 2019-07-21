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


private fun VisualObject.applyPosition(pos: GDMLPosition): VisualObject = apply {
    x = pos.x
    y = pos.y
    z = pos.z
    //TODO convert units if needed
}

private fun VisualObject.applyRotation(rotation: GDMLRotation): VisualObject = apply {
    rotationX = rotation.x
    rotationY = rotation.y
    rotationZ = rotation.z
    //TODO convert units if needed
}

private fun VisualGroup.addSolid(root: GDML, solid: GDMLSolid, block: VisualObject.() -> Unit = {}): VisualObject {
    return when (solid) {
        is GDMLBox -> box(solid.x, solid.y, solid.z)
        is GDMLTube -> cylinder(solid.rmax, solid.z) {
            startAngle = solid.startphi
            angle = solid.deltaphi
        }
        is GDMLXtru -> extrude {
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
        is GDMLSphere -> sphere(solid.rmax, solid.deltaphi, solid.deltatheta) {
            phiStart = solid.startphi.toDouble()
            thetaStart = solid.starttheta.toDouble()
        }
        is GDMLOrb -> sphere(solid.r)
        is GDMLPolyhedra -> extrude {
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
            return composite(type) {
                addSolid(root, first) {
                    solid.resolveFirstPosition(root)?.let { applyPosition(it) }
                    solid.resolveFirstRotation(root)?.let { applyRotation(it) }
                }
                addSolid(root, second) {}
                solid.resolvePosition(root)?.let { applyPosition(it) }
                solid.resolveRotation(root)?.let { applyRotation(it) }
            }
        }
    }.apply(block)
}

private fun VisualGroup.addVolume(
    root: GDML,
    gdmlVolume: GDMLVolume,
    resolveColor: GDMLMaterial.() -> Meta
): VisualGroup {
    val solid =
        gdmlVolume.solidref.resolve(root)
            ?: error("Solid with tag ${gdmlVolume.solidref.ref} for volume ${gdmlVolume.name} not defined")
    val material =
        gdmlVolume.materialref.resolve(root)
            ?: error("Material with tag ${gdmlVolume.materialref.ref} for volume ${gdmlVolume.name} not defined")

    addSolid(root, solid) {
        color(material.resolveColor())
    }

    gdmlVolume.physVolumes.forEach {
        val volume = it.volumeref.resolve(root) ?: error("Volume with ref ${it.volumeref.ref} could not be resolved")
        addVolume(root, volume, resolveColor).apply {
            it.resolvePosition(root)?.let { pos -> applyPosition(pos) }
            it.resolveRotation(root)?.let { rot -> applyRotation(rot) }
        }
    }

    return this
}


fun GDML.toVisual(): VisualGroup {
    //TODO add materials cache
    fun GDMLMaterial.color(): Meta = EmptyMeta
    return VisualGroup().also { it.addVolume(this, world) { color() } }
}