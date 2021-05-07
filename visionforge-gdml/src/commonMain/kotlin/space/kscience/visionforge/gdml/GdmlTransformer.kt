package space.kscience.visionforge.gdml

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaBuilder
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.names.toName
import space.kscience.gdml.*
import space.kscience.visionforge.set
import space.kscience.visionforge.setProperty
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.solid.SolidMaterial.Companion.MATERIAL_KEY
import space.kscience.visionforge.styleSheet
import space.kscience.visionforge.useStyle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val solidsName = "solids".asName()
private val volumesName = "volumes".asName()

@Suppress("NOTHING_TO_INLINE")
private inline operator fun Number.times(d: Double) = toDouble() * d

@Suppress("NOTHING_TO_INLINE")
private inline operator fun Number.times(f: Float) = toFloat() * f

public class GdmlTransformerSettings {
    public val random: Random = Random(222)

    public enum class Action {
        ADD,
        REJECT,
        PROTOTYPE
    }

    public var lUnit: LUnit = LUnit.MM
    public var aUnit: AUnit = AUnit.RADIAN

    public var solidAction: (GdmlSolid) -> Action = { Action.PROTOTYPE }
    public var volumeAction: (GdmlGroup) -> Action = { Action.PROTOTYPE }

    public var paint: SolidMaterial.(material: GdmlMaterial) -> Unit = { _ ->
        color(random.nextInt(16777216))
    }
}

private class GdmlTransformer(val settings: GdmlTransformerSettings) {
    //private val materialCache = HashMap<GdmlMaterial, Meta>()

    /**
     * A special group for local templates
     */
    private val proto = SolidGroup()

    private val solids = proto.group(solidsName) {
        setProperty("edges.enabled", false)
    }


    private val referenceStore = HashMap<Name, MutableList<SolidReferenceGroup>>()

    private fun proxySolid(root: Gdml, group: SolidGroup, solid: GdmlSolid, name: String): SolidReferenceGroup {
        val templateName = solidsName + name
        if (proto[templateName] == null) {
            solids.addSolid(root, solid, name)
        }
        val ref = group.ref(templateName, name)
        referenceStore.getOrPut(templateName) { ArrayList() }.add(ref)
        return ref
    }

    private fun proxyVolume(
        root: Gdml,
        group: SolidGroup,
        physVolume: GdmlPhysVolume,
        volume: GdmlGroup,
    ): SolidReferenceGroup {
        val templateName = volumesName + volume.name.asName()
        if (proto[templateName] == null) {
            proto[templateName] = volume(root, volume)
        }
        val ref = group.ref(templateName, physVolume.name).withPosition(root, physVolume)
        referenceStore.getOrPut(templateName) { ArrayList() }.add(ref)
        return ref
    }

    private val styleCache = HashMap<Name, Meta>()

    var solidConfiguration: Solid.(parent: GdmlVolume, solid: GdmlSolid) -> Unit = { parent, _ ->
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

    fun configureSolid(root: Gdml, obj: Solid, parent: GdmlVolume, solid: GdmlSolid) {
        val material = parent.materialref.resolve(root) ?: GdmlElement(parent.materialref.ref)

        val styleName = "materials.${material.name}"

        obj.useStyle(styleName) {
            val vfMaterial = settings.run { SolidMaterial().apply { paint(material) } }
            MATERIAL_KEY put vfMaterial.toMeta()
            "Gdml.material" put material.name
        }

        obj.solidConfiguration(parent, solid)
    }

    fun <T : Solid> T.withPosition(
        newPos: GdmlPosition? = null,
        newRotation: GdmlRotation? = null,
        newScale: GdmlScale? = null,
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

    fun <T : Solid> T.withPosition(root: Gdml, physVolume: GdmlPhysVolume): T = withPosition(
        physVolume.resolvePosition(root),
        physVolume.resolveRotation(root),
        physVolume.resolveScale(root)
    )

    private fun GdmlSolid.lscale(targetUnit: LUnit): Float {
        val solidUnit = lunit ?: return 1f
        return if (solidUnit == targetUnit) {
            1f
        } else {
            solidUnit.value / targetUnit.value
        }
    }

    private fun GdmlSolid.ascale(targetUnit: AUnit = AUnit.RAD): Float {
        val solidUnit = aunit ?: return 1f
        return if (solidUnit == targetUnit) {
            1f
        } else {
            solidUnit.value / targetUnit.value
        }
    }

    fun SolidGroup.addSolid(
        root: Gdml,
        solid: GdmlSolid,
        name: String? = null,
    ): Solid {
        //context.solidAdded(solid)
        val lScale = solid.lscale(settings.lUnit)
        val aScale = solid.ascale(settings.aUnit)
        return when (solid) {
            is GdmlBox -> box(solid.x * lScale, solid.y * lScale, solid.z * lScale, name)
            is GdmlTube -> tube(
                solid.rmax * lScale,
                solid.z * lScale,
                solid.rmin * lScale,
                solid.startphi * aScale,
                solid.deltaphi * aScale,
                name
            )
            is GdmlCone -> if (solid.rmin1.toDouble() == 0.0 && solid.rmin2.toDouble() == 0.0) {
                cone(
                    bottomRadius = solid.rmax1,
                    height = solid.z,
                    upperRadius = solid.rmax2,
                    name = name
                ) {
                    startAngle = solid.startphi.toFloat()
                    angle = solid.deltaphi.toFloat()
                }
            } else {
                coneSurface(
                    bottomOuterRadius = solid.rmax1,
                    bottomInnerRadius = solid.rmin1,
                    height = solid.z,
                    topOuterRadius = solid.rmax2,
                    topInnerRadius = solid.rmin2,
                    name = name
                ) {
                    startAngle = solid.startphi.toFloat()
                    angle = solid.deltaphi.toFloat()
                }
            }
            is GdmlXtru -> extrude(name) {
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
            is GdmlScaledSolid -> {
                //Add solid with modified scale
                val innerSolid: GdmlSolid = solid.solidref.resolve(root)
                    ?: error("Solid with tag ${solid.solidref.ref} for scaled solid ${solid.name} not defined")

                addSolid(root, innerSolid, name).apply {
                    scaleX *= solid.scale.x.toFloat()
                    scaleY *= solid.scale.y.toFloat()
                    scaleZ = solid.scale.z.toFloat()
                }
            }
            is GdmlSphere -> sphereLayer(solid.rmax * lScale, solid.rmin * lScale, name) {
                phi = solid.deltaphi * aScale
                theta = solid.deltatheta * aScale
                phiStart = solid.startphi * aScale
                thetaStart = solid.starttheta * aScale
            }
            is GdmlOrb -> sphere(solid.r * lScale, name = name)
            is GdmlPolyhedra -> extrude(name) {
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
            is GdmlBoolSolid -> {
                val first: GdmlSolid = solid.first.resolve(root) ?: error("")
                val second: GdmlSolid = solid.second.resolve(root) ?: error("")
                val type: CompositeType = when (solid) {
                    is GdmlUnion -> CompositeType.UNION
                    is GdmlSubtraction -> CompositeType.SUBTRACT
                    is GdmlIntersection -> CompositeType.INTERSECT
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
            is GdmlTrapezoid -> {
                val dxBottom = solid.x1.toDouble()
                val dxTop = solid.x2.toDouble()
                val dyBottom = solid.y1.toDouble()
                val dyTop = solid.y2.toDouble()
                val dz = solid.z.toDouble()
                val node1 = Point3D(-dxBottom, -dyBottom, -dz)
                val node2 = Point3D(dxBottom, -dyBottom, -dz)
                val node3 = Point3D(dxBottom, dyBottom, -dz)
                val node4 = Point3D(-dxBottom, dyBottom, -dz)
                val node5 = Point3D(-dxTop, -dyTop, dz)
                val node6 = Point3D(dxTop, -dyTop, dz)
                val node7 = Point3D(dxTop, dyTop, dz)
                val node8 = Point3D(-dxTop, dyTop, dz)
                hexagon(node1, node2, node3, node4, node5, node6, node7, node8, name)
            }
            is GdmlEllipsoid -> TODO("Renderer for $solid not supported yet")
            is GdmlElTube -> TODO("Renderer for $solid not supported yet")
            is GdmlElCone -> TODO("Renderer for $solid not supported yet")
            is GdmlParaboloid -> TODO("Renderer for $solid not supported yet")
            is GdmlParallelepiped -> TODO("Renderer for $solid not supported yet")
            is GdmlTorus -> TODO("Renderer for $solid not supported yet")
            is GdmlPolycone -> TODO("Renderer for $solid not supported yet")
        }
    }

    fun SolidGroup.addSolidWithCaching(
        root: Gdml,
        solid: GdmlSolid,
        name: String?,
    ): Solid? {
        require(name != "") { "Can't use empty solid name. Use null instead." }
        return when (settings.solidAction(solid)) {
            GdmlTransformerSettings.Action.ADD -> {
                addSolid(root, solid, name)
            }
            GdmlTransformerSettings.Action.PROTOTYPE -> {
                proxySolid(root, this, solid, name ?: solid.name)
            }
            GdmlTransformerSettings.Action.REJECT -> {
                //ignore
                null
            }
        }
    }

    fun SolidGroup.addPhysicalVolume(
        root: Gdml,
        physVolume: GdmlPhysVolume,
    ) {
        val volume: GdmlGroup = physVolume.volumeref.resolve(root)
            ?: error("Volume with ref ${physVolume.volumeref.ref} could not be resolved")

        // a special case for single solid volume
        if (volume is GdmlVolume && volume.physVolumes.isEmpty() && volume.placement == null) {
            val solid = volume.solidref.resolve(root)
                ?: error("Solid with tag ${volume.solidref.ref} for volume ${volume.name} not defined")
            addSolidWithCaching(root, solid, physVolume.name)?.apply {
                configureSolid(root, this, volume, solid)
                withPosition(root, physVolume)
            }
            return
        }

        when (settings.volumeAction(volume)) {
            GdmlTransformerSettings.Action.ADD -> {
                val group: SolidGroup = volume(root, volume)
                this[physVolume.name] = group.withPosition(root, physVolume)
            }
            GdmlTransformerSettings.Action.PROTOTYPE -> {
                proxyVolume(root, this, physVolume, volume)
            }
            GdmlTransformerSettings.Action.REJECT -> {
                //ignore
            }
        }
    }

    fun SolidGroup.addDivisionVolume(
        root: Gdml,
        divisionVolume: GdmlDivisionVolume,
    ) {
        val volume: GdmlGroup = divisionVolume.volumeref.resolve(root)
            ?: error("Volume with ref ${divisionVolume.volumeref.ref} could not be resolved")

        //TODO add divisions
        set(null, volume(root, volume))
    }

    private fun volume(
        root: Gdml,
        group: GdmlGroup,
    ): SolidGroup = SolidGroup().apply {
        if (group is GdmlVolume) {
            val solid: GdmlSolid = group.solidref.resolve(root)
                ?: error("Solid with tag ${group.solidref.ref} for volume ${group.name} not defined")

            addSolidWithCaching(root, solid, null)?.apply {
                configureSolid(root, this, group, solid)
            }

            when (val vol: GdmlPlacement? = group.placement) {
                is GdmlPhysVolume -> addPhysicalVolume(root, vol)
                is GdmlDivisionVolume -> addDivisionVolume(root, vol)
            }
        }

        group.physVolumes.forEach { physVolume ->
            addPhysicalVolume(root, physVolume)
        }
    }

    private fun finalize(final: SolidGroup): SolidGroup {
        //final.prototypes = proto
        final.useStyle("gdml") {
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

    fun transform(root: Gdml): SolidGroup =
        finalize(volume(root, root.world.resolve(root) ?: error("GDML root is not resolved")))
}


public fun Gdml.toVision(block: GdmlTransformerSettings.() -> Unit = {}): SolidGroup {
    val context = GdmlTransformer(GdmlTransformerSettings().apply(block))
    return context.transform(this)
}

/**
 * Append Gdml node to the group
 */
public fun SolidGroup.gdml(gdml: Gdml, key: String? = null, transformer: GdmlTransformerSettings.() -> Unit = {}) {
    val visual = gdml.toVision(transformer)
    //println(Visual3DPlugin.json.stringify(VisualGroup3D.serializer(), visual))
    set(key, visual)
}