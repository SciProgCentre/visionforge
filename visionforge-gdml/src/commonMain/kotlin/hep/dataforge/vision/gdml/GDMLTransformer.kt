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
import hep.dataforge.vision.useStyle
import kscience.gdml.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val solidsName = "solids".asName()
private val volumesName = "volumes".asName()

public class GDMLTransformer internal constructor(public val root: GDML) {
    //private val materialCache = HashMap<GDMLMaterial, Meta>()
    private val random = Random(222)

    public enum class Action {
        ADD,
        REJECT,
        PROTOTYPE
    }

    public var lUnit: LUnit = LUnit.MM
    public var aUnit: AUnit = AUnit.RADIAN

    public var solidAction: (GDMLSolid) -> Action = { Action.PROTOTYPE }
    public var volumeAction: (GDMLGroup) -> Action = { Action.PROTOTYPE }

    /**
     * A special group for local templates
     */
    private val proto by lazy { SolidGroup() }

    private val solids by lazy {
        proto.group(solidsName) {
            config["edges.enabled"] = false
        }
    }

    private val referenceStore = HashMap<Name, MutableList<Proxy>>()

    private fun proxySolid(group: SolidGroup, solid: GDMLSolid, name: String): Proxy {
        val templateName = solidsName + name
        if (proto[templateName] == null) {
            solids.addSolid(solid, name)
        }
        val ref = group.ref(templateName, name)
        referenceStore.getOrPut(templateName) { ArrayList() }.add(ref)
        return ref
    }

    private fun proxyVolume(group: SolidGroup, physVolume: GDMLPhysVolume, volume: GDMLGroup): Proxy {
        val templateName = volumesName + volume.name.asName()
        if (proto[templateName] == null) {
            proto[templateName] = volume(volume)
        }
        val ref = group.ref(templateName, physVolume.name ?: "").withPosition(physVolume)
        referenceStore.getOrPut(templateName) { ArrayList() }.add(ref)
        return ref
    }

    private val styleCache = HashMap<Name, Meta>()

    public var solidConfiguration: Solid.(parent: GDMLVolume, solid: GDMLSolid) -> Unit = { parent, _ ->
        lUnit = LUnit.CM
        if (parent.physVolumes.isNotEmpty()) {
            useStyle("opaque") {
                SolidMaterial.MATERIAL_OPACITY_KEY put 0.3
                "edges.enabled" put true
            }
        }
    }

    private fun Solid.useStyle(name: String, builder: MetaBuilder.() -> Unit) {
        styleCache.getOrPut(name.toName()) {
            Meta(builder)
        }
        useStyle(name)
    }

    private fun configureSolid(obj: Solid, parent: GDMLVolume, solid: GDMLSolid) {
        val material = parent.materialref.resolve(root) ?: GDMLElement(parent.materialref.ref)

        val styleName = "material[${material.name}]"

        obj.useStyle(styleName) {
            MATERIAL_COLOR_KEY put random.nextInt(16777216)
            "gdml.material" put material.name
        }

        obj.solidConfiguration(parent, solid)
    }

    public var onFinish: GDMLTransformer.() -> Unit = {}


    private fun <T : Solid> T.withPosition(
        newPos: GDMLPosition? = null,
        newRotation: GDMLRotation? = null,
        newScale: GDMLScale? = null
    ): T = apply {
        newPos?.let {
            val point = Point3D(it.x(lUnit), it.y(lUnit), it.z(lUnit))
            if (position != null || point != World.ZERO) {
                position = point
            }
        }
        newRotation?.let {
            val point = Point3D(it.x(aUnit), it.y(aUnit), it.z(aUnit))
            if (rotation != null || point != World.ZERO) {
                rotation = point
            }
            //this@withPosition.rotationOrder = RotationOrder.ZXY
        }
        newScale?.let {
            val point = Point3D(it.x, it.y, it.z)
            if (scale != null || point != World.ONE) {
                scale = point
            }
        }
        //TODO convert units if needed
    }

    private fun <T : Solid> T.withPosition(physVolume: GDMLPhysVolume): T = withPosition(
        physVolume.resolvePosition(root),
        physVolume.resolveRotation(root),
        physVolume.resolveScale(root)
    )

    @Suppress("NOTHING_TO_INLINE")
    private inline operator fun Number.times(d: Double) = toDouble() * d

    @Suppress("NOTHING_TO_INLINE")
    private inline operator fun Number.times(f: Float) = toFloat() * f

    private fun SolidGroup.addSolid(
        solid: GDMLSolid,
        name: String = ""
    ): Solid {
        //context.solidAdded(solid)
        val lScale = solid.lscale(lUnit)
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

                addSolid(innerSolid, name).apply {
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
                    addSolid(first).withPosition(
                        solid.resolveFirstPosition(root),
                        solid.resolveFirstRotation(root),
                        null
                    )

                    addSolid(second).withPosition(
                        solid.resolvePosition(root),
                        solid.resolveRotation(root),
                        null
                    )

                }
            }
            else -> error("Renderer for $solid not supported yet")
        }
    }

    private fun SolidGroup.addSolidWithCaching(
        solid: GDMLSolid,
        name: String = solid.name
    ): Solid? {
        return when (solidAction(solid)) {
            Action.ADD -> {
                addSolid(solid, name)
            }
            Action.PROTOTYPE -> {
                proxySolid(this, solid, name)
            }
            Action.REJECT -> {
                //ignore
                null
            }
        }
    }

    private fun SolidGroup.addPhysicalVolume(
        physVolume: GDMLPhysVolume
    ) {
        val volume: GDMLGroup = physVolume.volumeref.resolve(root)
            ?: error("Volume with ref ${physVolume.volumeref.ref} could not be resolved")

        // a special case for single solid volume
        if (volume is GDMLVolume && volume.physVolumes.isEmpty() && volume.placement == null) {
            val solid = volume.solidref.resolve(root)
                ?: error("Solid with tag ${volume.solidref.ref} for volume ${volume.name} not defined")
            addSolidWithCaching(solid, physVolume.name ?: "")?.apply {
                configureSolid(this, volume, solid)
                withPosition(physVolume)
            }
            return
        }

        when (volumeAction(volume)) {
            Action.ADD -> {
                val group: SolidGroup = volume(volume)
                this[physVolume.name ?: ""] = group.withPosition(physVolume)
            }
            Action.PROTOTYPE -> {
                proxyVolume(this, physVolume, volume)
            }
            Action.REJECT -> {
                //ignore
            }
        }
    }

    private fun SolidGroup.addDivisionVolume(
        divisionVolume: GDMLDivisionVolume
    ) {
        val volume: GDMLGroup = divisionVolume.volumeref.resolve(root)
            ?: error("Volume with ref ${divisionVolume.volumeref.ref} could not be resolved")

        //TODO add divisions
        set(Name.EMPTY, volume(volume))
    }

    private fun volume(
        group: GDMLGroup
    ): SolidGroup = SolidGroup().apply {
        if (group is GDMLVolume) {
            val solid: GDMLSolid = group.solidref.resolve(root)
                ?: error("Solid with tag ${group.solidref.ref} for volume ${group.name} not defined")

            addSolidWithCaching(solid)?.apply {
                configureSolid(this, group, solid)
            }

            when (val vol: GDMLPlacement? = group.placement) {
                is GDMLPhysVolume -> addPhysicalVolume(vol)
                is GDMLDivisionVolume -> addDivisionVolume(vol)
            }
        }

        group.physVolumes.forEach { physVolume ->
            addPhysicalVolume(physVolume)
        }
    }

    private fun finalize(final: SolidGroup): SolidGroup {
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
                set(token.asName(), item)
            }
        }
        styleCache.forEach {
            final.styleSheet {
                define(it.key.toString(), it.value)
            }
        }
        onFinish(this@GDMLTransformer)
        return final
    }

    public val result: SolidGroup by lazy {
        finalize(volume(root.world))
    }
}


public fun GDML.toVision(block: GDMLTransformer.() -> Unit = {}): SolidGroup {
    val context = GDMLTransformer(this).apply(block)
    return context.result
}

/**
 * Append gdml node to the group
 */
public fun SolidGroup.gdml(gdml: GDML, key: String = "", transformer: GDMLTransformer.() -> Unit = {}) {
    val visual = gdml.toVision(transformer)
    //println(Visual3DPlugin.json.stringify(VisualGroup3D.serializer(), visual))
    set(key, visual)
}