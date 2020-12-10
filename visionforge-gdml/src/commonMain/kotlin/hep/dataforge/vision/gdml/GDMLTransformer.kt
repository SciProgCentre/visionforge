package hep.dataforge.vision.gdml

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.set
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.names.toName
import hep.dataforge.vision.set
import hep.dataforge.vision.solid.*
import hep.dataforge.vision.solid.SolidMaterial.Companion.MATERIAL_COLOR_KEY
import hep.dataforge.vision.styleSheet
import hep.dataforge.vision.useStyle
import kscience.gdml.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val solidsName = "solids".asName()
private val volumesName = "volumes".asName()

@Suppress("NOTHING_TO_INLINE")
private inline operator fun Number.times(d: Double) = toDouble() * d

@Suppress("NOTHING_TO_INLINE")
private inline operator fun Number.times(f: Float) = toFloat() * f

public class GDMLTransformerSettings {
    public enum class Action {
        ADD,
        REJECT,
        PROTOTYPE
    }

    public var lUnit: LUnit = LUnit.CM
    public var aUnit: AUnit = AUnit.RADIAN

    public var solidAction: (GDMLSolid) -> Action = { Action.PROTOTYPE }
    public var volumeAction: (GDMLGroup) -> Action = { Action.PROTOTYPE }

}

private class GDMLTransformer(val settings: GDMLTransformerSettings) {
    //private val materialCache = HashMap<GDMLMaterial, Meta>()
    private val random = Random(222)

    /**
     * A special group for local templates
     */
    private val proto = SolidGroup()

    private val solids = proto.group(solidsName) {
        config["edges.enabled"] = false
    }


    private val referenceStore = HashMap<Name, MutableList<SolidReference>>()

    private fun proxySolid(root: GDML, group: SolidGroup, solid: GDMLSolid, name: String): SolidReference {
        val templateName = solidsName + name
        if (proto[templateName] == null) {
            solids.addSolid(root, solid, name)
        }
        val ref = group.ref(templateName, name)
        referenceStore.getOrPut(templateName) { ArrayList() }.add(ref)
        return ref
    }

    private fun proxyVolume(root: GDML, group: SolidGroup, physVolume: GDMLPhysVolume, volume: GDMLGroup): SolidReference {
        val templateName = volumesName + volume.name.asName()
        if (proto[templateName] == null) {
            proto[templateName] = volume(root, volume)
        }
        val ref = group.ref(templateName, physVolume.name ?: "").withPosition(root, physVolume)
        referenceStore.getOrPut(templateName) { ArrayList() }.add(ref)
        return ref
    }

    private val styleCache = HashMap<Name, Meta>()

    var solidConfiguration: Solid.(parent: GDMLVolume, solid: GDMLSolid) -> Unit = { parent, _ ->
        if (parent.physVolumes.isNotEmpty()) {
            useStyle("opaque") {
                SolidMaterial.MATERIAL_OPACITY_KEY put 0.3
                "edges.enabled" put true
            }
        }
    }

    fun Solid.useStyle(name: String, builder: MetaBuilder.() -> Unit) {
        styleCache.getOrPut(name.toName()) {
            Meta(builder)
        }
        useStyle(name)
    }

    fun configureSolid(root: GDML, obj: Solid, parent: GDMLVolume, solid: GDMLSolid) {
        val material = parent.materialref.resolve(root) ?: GDMLElement(parent.materialref.ref)

        val styleName = "material[${material.name}]"

        obj.useStyle(styleName) {
            MATERIAL_COLOR_KEY put random.nextInt(16777216)
            "gdml.material" put material.name
        }

        obj.solidConfiguration(parent, solid)
    }

    fun <T : Solid> T.withPosition(
        newPos: GDMLPosition? = null,
        newRotation: GDMLRotation? = null,
        newScale: GDMLScale? = null,
    ): T = apply {
        newPos?.let {
            val point = Point3D(it.x(settings.lUnit), it.y(settings.lUnit), it.z(settings.lUnit))
            if (position != null || point != Point3D.ZERO) {
                position = point
            }
        }
        newRotation?.let {
            val point = Point3D(it.x(settings.aUnit), it.y(settings.aUnit), it.z(settings.aUnit))
            if (rotation != null || point != Point3D.ZERO) {
                rotation = point
            }
            //this@withPosition.rotationOrder = RotationOrder.ZXY
        }
        newScale?.let {
            val point = Point3D(it.x, it.y, it.z)
            if (scale != null || point != Point3D.ONE) {
                scale = point
            }
        }
        //TODO convert units if needed
    }

    fun <T : Solid> T.withPosition(root: GDML, physVolume: GDMLPhysVolume): T = withPosition(
        physVolume.resolvePosition(root),
        physVolume.resolveRotation(root),
        physVolume.resolveScale(root)
    )

    fun SolidGroup.addSolid(
        root: GDML,
        solid: GDMLSolid,
        name: String = "",
    ): Solid {
        //context.solidAdded(solid)
        val lScale = solid.lscale(settings.lUnit)
        val aScale = solid.ascale()
        return when (solid) {
            is GDMLBox -> box(solid.x * lScale, solid.y * lScale, solid.z * lScale, name)
            is GDMLTube -> tube(
                solid.rmax * lScale,
                solid.z * lScale,
                solid.rmin * lScale,
                solid.startphi * aScale,
                solid.deltaphi * aScale,
                name
            )
            is GDMLCone -> cone(solid.rmax1, solid.z, solid.rmax2, name = name) {
                require(solid.rmin1 == 0.0) { "Empty cones are not supported" }
                require(solid.rmin2 == 0.0) { "Empty cones are not supported" }
                startAngle = solid.startphi.toFloat()
                angle = solid.deltaphi.toFloat()
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
                val innerSolid: GDMLSolid = solid.solidref.resolve(root)
                    ?: error("Solid with tag ${solid.solidref.ref} for scaled solid ${solid.name} not defined")

                addSolid(root, innerSolid, name).apply {
                    scaleX *= solid.scale.x.toFloat()
                    scaleY *= solid.scale.y.toFloat()
                    scaleZ = solid.scale.z.toFloat()
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
                val first: GDMLSolid = solid.first.resolve(root) ?: error("")
                val second: GDMLSolid = solid.second.resolve(root) ?: error("")
                val type: CompositeType = when (solid) {
                    is GDMLUnion -> CompositeType.UNION
                    is GDMLSubtraction -> CompositeType.SUBTRACT
                    is GDMLIntersection -> CompositeType.INTERSECT
                }

                return composite(type, name) {
                    addSolid(root, first).withPosition(
                        solid.resolveFirstPosition(root),
                        solid.resolveFirstRotation(root),
                        null
                    )

                    addSolid(root, second).withPosition(
                        solid.resolvePosition(root),
                        solid.resolveRotation(root),
                        null
                    )

                }
            }
            else -> error("Renderer for $solid not supported yet")
        }
    }

    fun SolidGroup.addSolidWithCaching(
        root: GDML,
        solid: GDMLSolid,
        name: String = solid.name,
    ): Solid? {
        return when (settings.solidAction(solid)) {
            GDMLTransformerSettings.Action.ADD -> {
                addSolid(root, solid, name)
            }
            GDMLTransformerSettings.Action.PROTOTYPE -> {
                proxySolid(root, this, solid, name)
            }
            GDMLTransformerSettings.Action.REJECT -> {
                //ignore
                null
            }
        }
    }

    fun SolidGroup.addPhysicalVolume(
        root: GDML,
        physVolume: GDMLPhysVolume,
    ) {
        val volume: GDMLGroup = physVolume.volumeref.resolve(root)
            ?: error("Volume with ref ${physVolume.volumeref.ref} could not be resolved")

        // a special case for single solid volume
        if (volume is GDMLVolume && volume.physVolumes.isEmpty() && volume.placement == null) {
            val solid = volume.solidref.resolve(root)
                ?: error("Solid with tag ${volume.solidref.ref} for volume ${volume.name} not defined")
            addSolidWithCaching(root, solid, physVolume.name ?: "")?.apply {
                configureSolid(root, this, volume, solid)
                withPosition(root, physVolume)
            }
            return
        }

        when (settings.volumeAction(volume)) {
            GDMLTransformerSettings.Action.ADD -> {
                val group: SolidGroup = volume(root, volume)
                this[physVolume.name ?: ""] = group.withPosition(root, physVolume)
            }
            GDMLTransformerSettings.Action.PROTOTYPE -> {
                proxyVolume(root, this, physVolume, volume)
            }
            GDMLTransformerSettings.Action.REJECT -> {
                //ignore
            }
        }
    }

    fun SolidGroup.addDivisionVolume(
        root: GDML,
        divisionVolume: GDMLDivisionVolume,
    ) {
        val volume: GDMLGroup = divisionVolume.volumeref.resolve(root)
            ?: error("Volume with ref ${divisionVolume.volumeref.ref} could not be resolved")

        //TODO add divisions
        set(Name.EMPTY, volume(root, volume))
    }

    private fun volume(
        root: GDML,
        group: GDMLGroup,
    ): SolidGroup = SolidGroup().apply {
        if (group is GDMLVolume) {
            val solid: GDMLSolid = group.solidref.resolve(root)
                ?: error("Solid with tag ${group.solidref.ref} for volume ${group.name} not defined")

            addSolidWithCaching(root, solid)?.apply {
                configureSolid(root, this, group, solid)
            }

            when (val vol: GDMLPlacement? = group.placement) {
                is GDMLPhysVolume -> addPhysicalVolume(root, vol)
                is GDMLDivisionVolume -> addDivisionVolume(root, vol)
            }
        }

        group.physVolumes.forEach { physVolume ->
            addPhysicalVolume(root, physVolume)
        }
    }

    fun finalize(final: SolidGroup): SolidGroup {
        //final.prototypes = proto
        final.useStyle("GDML") {
            Solid.ROTATION_ORDER_KEY put RotationOrder.ZXY
        }

        //inline prototypes
//        referenceStore.forEach { (protoName, list) ->
//            val proxy = list.singleOrNull() ?: return@forEach
//            val parent = proxy.parent as? MutableVisionGroup ?: return@forEach
//            val token = parent.children.entries.find { it.value == proxy }?.key ?: error("Inconsistent reference cache")
//            val prototype = proto[protoName] as? Solid ?:  error("Inconsistent reference cache")
//            prototype.parent = null
//            parent[token] = prototype
//            prototype.updateFrom(proxy)
//
//            //FIXME update prototype
//            proto[protoName] = null
//        }

        final.prototypes {
            proto.children.forEach { (token, item) ->
                item.parent = null
                set(token.asName(), item as? Solid)
            }
        }
        styleCache.forEach {
            final.styleSheet {
                define(it.key.toString(), it.value)
            }
        }
        return final
    }

    fun transform(root: GDML): SolidGroup = finalize(volume(root, root.world))
}


public fun GDML.toVision(block: GDMLTransformerSettings.() -> Unit = {}): SolidGroup {
    val context = GDMLTransformer(GDMLTransformerSettings().apply(block))
    return context.transform(this)
}

/**
 * Append gdml node to the group
 */
public fun SolidGroup.gdml(gdml: GDML, key: String = "", transformer: GDMLTransformerSettings.() -> Unit = {}) {
    val visual = gdml.toVision(transformer)
    //println(Visual3DPlugin.json.stringify(VisualGroup3D.serializer(), visual))
    set(key, visual)
}