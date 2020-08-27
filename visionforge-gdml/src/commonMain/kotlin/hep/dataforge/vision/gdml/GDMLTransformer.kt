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
import scientifik.gdml.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val solidsName = "solids".asName()
private val volumesName = "volumes".asName()

class GDMLTransformer(val root: GDML) {
    //private val materialCache = HashMap<GDMLMaterial, Meta>()
    private val random = Random(222)

    enum class Action {
        ADD,
        REJECT,
        PROTOTYPE
    }

    var lUnit: LUnit = LUnit.MM
    var aUnit: AUnit = AUnit.RADIAN

    var solidAction: (GDMLSolid) -> Action = { Action.PROTOTYPE }
    var volumeAction: (GDMLGroup) -> Action = { Action.PROTOTYPE }

    /**
     * A special group for local templates
     */
    internal val proto by lazy { SolidGroup() }

    internal val solids by lazy {
        proto.group(solidsName) {
            config["edges.enabled"] = false
        }
    }

    internal val volumes by lazy {
        proto.group(volumesName)
    }

//    fun proxySolid(group: SolidGroup, solid: GDMLSolid, name: String): Proxy {
//        val fullName = solidsName + name
//        if (proto[fullName] == null) {
//            solids.addSolid(this, solid, name)
//        }
//        return group.ref(fullName, name)
//    }

    private val styleCache = HashMap<Name, Meta>()

    var solidConfiguration: Solid.(parent: GDMLVolume, solid: GDMLSolid) -> Unit = { parent, _ ->
        lUnit = LUnit.CM
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

    internal fun configureSolid(obj: Solid, parent: GDMLVolume, solid: GDMLSolid) {
        val material = parent.materialref.resolve(root) ?: GDMLElement(parent.materialref.ref)

        val styleName = "material[${material.name}]"

        obj.useStyle(styleName) {
            MATERIAL_COLOR_KEY put random.nextInt(16777216)
            "gdml.material" put material.name
        }

        obj.solidConfiguration(parent, solid)
    }

    var onFinish: GDMLTransformer.() -> Unit = {}

    internal fun finalize(final: SolidGroup): SolidGroup {
        //final.prototypes = proto
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
        final.rotationOrder = RotationOrder.ZXY
        onFinish(this@GDMLTransformer)
        return final
    }

}

private fun Solid.withPosition(
    lUnit: LUnit,
    aUnit: AUnit = AUnit.RADIAN,
    newPos: GDMLPosition? = null,
    newRotation: GDMLRotation? = null,
    newScale: GDMLScale? = null
): Solid = apply {
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

private fun Solid.withPosition(context: GDMLTransformer, physVolume: GDMLPhysVolume) = withPosition(
    context.lUnit, context.aUnit,
    physVolume.resolvePosition(context.root),
    physVolume.resolveRotation(context.root),
    physVolume.resolveScale(context.root)
)

@Suppress("NOTHING_TO_INLINE")
private inline operator fun Number.times(d: Double) = toDouble() * d

@Suppress("NOTHING_TO_INLINE")
private inline operator fun Number.times(f: Float) = toFloat() * f

private fun SolidGroup.addSolid(
    context: GDMLTransformer,
    solid: GDMLSolid,
    name: String = ""
): Solid {
    //context.solidAdded(solid)
    val lScale = solid.lscale(context.lUnit)
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
            val innerSolid: GDMLSolid = solid.solidref.resolve(context.root)
                ?: error("Solid with tag ${solid.solidref.ref} for scaled solid ${solid.name} not defined")

            addSolid(context, innerSolid, name).apply {
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
            val first: GDMLSolid = solid.first.resolve(context.root) ?: error("")
            val second: GDMLSolid = solid.second.resolve(context.root) ?: error("")
            val type: CompositeType = when (solid) {
                is GDMLUnion -> CompositeType.UNION
                is GDMLSubtraction -> CompositeType.SUBTRACT
                is GDMLIntersection -> CompositeType.INTERSECT
            }

            return composite(type, name) {
                addSolid(context, first).withPosition(
                    context.lUnit, context.aUnit,
                    solid.resolveFirstPosition(context.root),
                    solid.resolveFirstRotation(context.root),
                    null
                )

                addSolid(context, second).withPosition(
                    context.lUnit, context.aUnit,
                    solid.resolvePosition(context.root),
                    solid.resolveRotation(context.root),
                    null
                )

            }
        }
        else -> error("Renderer for $solid not supported yet")
    }
}

private fun SolidGroup.addSolidWithCaching(
    context: GDMLTransformer,
    solid: GDMLSolid,
    name: String = solid.name
): Solid? {
    return when (context.solidAction(solid)) {
        GDMLTransformer.Action.ADD -> {
            addSolid(context, solid, name)
        }
        GDMLTransformer.Action.PROTOTYPE -> {
//            context.proxySolid(this, solid, name)
            val fullName = solidsName + solid.name.asName()
            if (context.proto[fullName] == null) {
                context.solids.addSolid(context, solid, solid.name)
            }
            ref(fullName, name)
        }
        GDMLTransformer.Action.REJECT -> {
            //ignore
            null
        }
    }
}

private fun SolidGroup.addPhysicalVolume(
    context: GDMLTransformer,
    physVolume: GDMLPhysVolume
) {
    val volume: GDMLGroup = physVolume.volumeref.resolve(context.root)
        ?: error("Volume with ref ${physVolume.volumeref.ref} could not be resolved")

    // a special case for single solid volume
    if (volume is GDMLVolume && volume.physVolumes.isEmpty() && volume.placement == null) {
        val solid = volume.solidref.resolve(context.root)
            ?: error("Solid with tag ${volume.solidref.ref} for volume ${volume.name} not defined")
        addSolidWithCaching(context, solid, physVolume.name ?: "")?.apply {
            context.configureSolid(this, volume, solid)
            withPosition(context, physVolume)
        }
        return
    }

    when (context.volumeAction(volume)) {
        GDMLTransformer.Action.ADD -> {
            val group: SolidGroup = volume(context, volume)
            this[physVolume.name ?: ""] = group.withPosition(context, physVolume)
        }
        GDMLTransformer.Action.PROTOTYPE -> {
            val fullName = volumesName + volume.name.asName()
            if (context.proto[fullName] == null) {
                context.proto[fullName] = volume(context, volume)
            }
            ref(fullName, physVolume.name ?: "").withPosition(context, physVolume)
        }
        GDMLTransformer.Action.REJECT -> {
            //ignore
        }
    }
}

private fun SolidGroup.addDivisionVolume(
    context: GDMLTransformer,
    divisionVolume: GDMLDivisionVolume
) {
    val volume: GDMLGroup = divisionVolume.volumeref.resolve(context.root)
        ?: error("Volume with ref ${divisionVolume.volumeref.ref} could not be resolved")

    //TODO add divisions
    set(Name.EMPTY, volume(context, volume))
}

private fun volume(
    context: GDMLTransformer,
    group: GDMLGroup
): SolidGroup = SolidGroup().apply {
    if (group is GDMLVolume) {
        val solid: GDMLSolid = group.solidref.resolve(context.root)
            ?: error("Solid with tag ${group.solidref.ref} for volume ${group.name} not defined")

        addSolidWithCaching(context, solid)?.apply {
            context.configureSolid(this, group, solid)
        }

        when (val vol: GDMLPlacement? = group.placement) {
            is GDMLPhysVolume -> addPhysicalVolume(context, vol)
            is GDMLDivisionVolume -> addDivisionVolume(context, vol)
        }
    }

    group.physVolumes.forEach { physVolume ->
        addPhysicalVolume(context, physVolume)
    }
}

fun GDML.toVision(block: GDMLTransformer.() -> Unit = {}): SolidGroup {
    val context = GDMLTransformer(this).apply(block)
    return context.finalize(volume(context, world))
}

/**
 * Append gdml node to the group
 */
fun SolidGroup.gdml(gdml: GDML, key: String = "", transformer: GDMLTransformer.() -> Unit = {}) {
    val visual = gdml.toVision(transformer)
    //println(Visual3DPlugin.json.stringify(VisualGroup3D.serializer(), visual))
    set(key, visual)
}