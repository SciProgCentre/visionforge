package hep.dataforge.vis.spatial.gdml

import hep.dataforge.meta.Meta
import hep.dataforge.meta.buildMeta
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.color
import hep.dataforge.vis.spatial.*
import scientifik.gdml.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


private fun VisualObject.withPosition(
    lUnit: LUnit,
    pos: GDMLPosition? = null,
    rotation: GDMLRotation? = null,
    scale: GDMLScale? = null
): VisualObject = apply {
    pos?.let {
        x = pos.x(lUnit)
        y = pos.y(lUnit)
        z = pos.z(lUnit)
    }
    rotation?.let {
        rotationX = rotation.x()
        rotationY = rotation.y()
        rotationZ = rotation.z()
    }
    scale?.let {
        scaleX = scale.x
        scaleY = scale.y
        scaleZ = scale.z
    }
    //TODO convert units if needed
}

private inline operator fun Number.times(d: Double) = toDouble() * d


private fun VisualGroup.addSolid(
    root: GDML,
    solid: GDMLSolid,
    lUnit: LUnit,
    name: String? = null,
    block: VisualObject.() -> Unit = {}
): VisualObject {
    val lScale = solid.lscale(lUnit)
    val aScale = solid.ascale()
    return when (solid) {
        is GDMLBox -> box(solid.x * lScale, solid.y * lScale, solid.z * lScale, name)
        is GDMLTube -> cylinder(solid.rmax * lScale, solid.z * lScale, name) {
            startAngle = solid.startphi * aScale
            angle = solid.deltaphi * aScale
        }
        is GDMLXtru -> extrude(name) {
            shape {
                solid.vertices.forEach {
                    point(it.x * lScale, it.y * lScale)
                }
            }
            solid.sections.sortedBy { it.zOrder }.forEach { section ->
                layer(
                    section.zPosition * lScale,
                    section.xOffset * lScale,
                    section.yOffset * lScale,
                    section.scalingFactor
                )
            }
        }
        is GDMLScaledSolid -> {
            //Add solid with modified scale
            val innerSolid = solid.solidref.resolve(root)
                ?: error("Solid with tag ${solid.solidref.ref} for scaled solid ${solid.name} not defined")

            addSolid(root, innerSolid, lUnit) {
                block()
                scaleX = scaleX.toDouble() * solid.scale.x.toDouble()
                scaleY = scaleY.toDouble() * solid.scale.y.toDouble()
                scaleZ = scaleZ.toDouble() * solid.scale.z.toDouble()
            }
        }
        is GDMLSphere -> sphere(solid.rmax * lScale, solid.deltaphi * aScale, solid.deltatheta * aScale, name) {
            phiStart = solid.startphi * aScale
            thetaStart = solid.starttheta * aScale
        }
        is GDMLOrb -> sphere(solid.r * lScale, name = name)
        is GDMLPolyhedra -> extrude(name) {
            //getting the radius of first
            require(solid.planes.size > 1) { "The polyhedron geometry requires at least two planes" }
            val baseRadius = solid.planes.first().rmax * lScale
            shape {
                (0..solid.numsides).forEach {
                    val phi = solid.deltaphi * aScale / solid.numsides * it + solid.startphi * aScale
                    (baseRadius * cos(phi) to baseRadius * sin(phi))
                }
            }
            solid.planes.forEach { plane ->
                //scaling all radii relative to first layer radius
                layer(plane.z * lScale, scale = plane.rmax * lScale / baseRadius)
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
                addSolid(root, first, lUnit) {
                    withPosition(lUnit, solid.resolveFirstPosition(root), solid.resolveFirstRotation(root), null)
                }
                addSolid(root, second, lUnit) {
                    withPosition(lUnit, solid.resolvePosition(root), solid.resolveRotation(root), null)
                }
            }
        }
    }.apply(block)
}

private fun VisualGroup.addPhysicalVolume(
    root: GDML,
    physVolume: GDMLPhysVolume,
    lUnit: LUnit,
    resolveColor: GDMLMaterial.() -> Meta
) {
    val volume: GDMLGroup = physVolume.volumeref.resolve(root)
        ?: error("Volume with ref ${physVolume.volumeref.ref} could not be resolved")

    addVolume(
        root,
        volume,
        lUnit,
        physVolume.resolvePosition(root),
        physVolume.resolveRotation(root),
        physVolume.resolveScale(root),
        resolveColor
    )
}

private fun VisualGroup.addDivisionVolume(
    root: GDML,
    divisionVolume: GDMLDivisionVolume,
    lUnit: LUnit,
    resolveColor: GDMLMaterial.() -> Meta
) {
    val volume: GDMLGroup = divisionVolume.volumeref.resolve(root)
        ?: error("Volume with ref ${divisionVolume.volumeref.ref} could not be resolved")

    //TODO add divisions
    addVolume(
        root,
        volume,
        lUnit,
        resolveColor = resolveColor
    )
}

private fun VisualGroup.addVolume(
    root: GDML,
    group: GDMLGroup,
    lUnit: LUnit,
    position: GDMLPosition? = null,
    rotation: GDMLRotation? = null,
    scale: GDMLScale? = null,
    resolveColor: GDMLMaterial.() -> Meta
) {

    group(group.name) {
        withPosition(lUnit, position, rotation, scale)

        if (group is GDMLVolume) {
            val solid = group.solidref.resolve(root)
                ?: error("Solid with tag ${group.solidref.ref} for volume ${group.name} not defined")
            val material = group.materialref.resolve(root) ?: GDMLElement(group.materialref.ref)
            //?: error("Material with tag ${group.materialref.ref} for volume ${group.name} not defined")

            addSolid(root, solid, lUnit, solid.name) {
                color(material.resolveColor())
            }

            when (val vol = group.placement) {
                is GDMLPhysVolume -> addPhysicalVolume(root, vol, lUnit, resolveColor)
                is GDMLDivisionVolume -> addDivisionVolume(root, vol, lUnit, resolveColor)
            }
        }

        group.physVolumes.forEach { physVolume ->
            addPhysicalVolume(root, physVolume, lUnit, resolveColor)
        }
    }
}

fun GDML.toVisual(lUnit: LUnit = LUnit.MM): VisualGroup {
    val cache = HashMap<GDMLMaterial, Meta>()
    val random = Random(111)

    fun GDMLMaterial.color(): Meta = cache.getOrPut(this) {
        buildMeta { "color" to random.nextInt(0, Int.MAX_VALUE) }
    }

    return VisualGroup().also { it.addVolume(this, world, lUnit) { color() } }
}