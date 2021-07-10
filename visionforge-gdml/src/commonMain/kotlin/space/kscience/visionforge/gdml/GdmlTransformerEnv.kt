package space.kscience.visionforge.gdml

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaBuilder
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.names.toName
import space.kscience.gdml.*
import space.kscience.visionforge.*
import space.kscience.visionforge.html.VisionOutput
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.solid.SolidMaterial.Companion.MATERIAL_KEY
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val solidsName = "solids".asName()
private val volumesName = "volumes".asName()

@Suppress("NOTHING_TO_INLINE")
private inline operator fun Number.times(d: Double) = toDouble() * d

@Suppress("NOTHING_TO_INLINE")
private inline operator fun Number.times(f: Float) = toFloat() * f

public class GdmlTransformer {

    public enum class Action {
        ADD,
        REJECT,
        PROTOTYPE
    }

    public var lUnit: LUnit = LUnit.MM
    public var aUnit: AUnit = AUnit.RADIAN

    public var solidAction: (GdmlSolid) -> Action = { Action.PROTOTYPE }
    public var volumeAction: (GdmlGroup) -> Action = { Action.PROTOTYPE }

    internal val styleCache = HashMap<Name, Meta>()

    public fun Solid.registerAndUseStyle(name: String, builder: MetaBuilder.() -> Unit) {
        styleCache.getOrPut(name.toName()) {
            Meta(builder)
        }
        useStyle(name)
    }

    public fun Solid.transparent() {
        registerAndUseStyle("transparent") {
            SolidMaterial.MATERIAL_OPACITY_KEY put 0.3
            "edges.enabled" put true
        }
    }

    /**
     * Configure paint for given solid with given [GdmlMaterial]
     */
    public var configurePaint: SolidMaterial.(material: GdmlMaterial, solid: GdmlSolid) -> Unit =
        { material, _ -> color(randomColor(material)) }
        private set

    public fun paint(block: SolidMaterial.(material: GdmlMaterial, solid: GdmlSolid) -> Unit) {
        configurePaint = block
    }

    /**
     * Configure given solid
     */
    public var configureSolid: Solid.(parent: GdmlVolume, solid: GdmlSolid, material: GdmlMaterial) -> Unit =
        { parent, solid, material ->
            val styleName = "materials.${material.name}"

            if (parent.physVolumes.isNotEmpty()) transparent()

            registerAndUseStyle(styleName) {
                val vfMaterial = SolidMaterial().apply {
                    configurePaint(material, solid)
                }
                MATERIAL_KEY put vfMaterial.toMeta()
                "Gdml.material" put material.name
            }
        }
        private set

    public fun configure(block: Solid.(parent: GdmlVolume, solid: GdmlSolid, material: GdmlMaterial) -> Unit) {
        val oldConfigure = configureSolid
        configureSolid = { parent: GdmlVolume, solid: GdmlSolid, material: GdmlMaterial ->
            oldConfigure(parent, solid, material)
            block(parent, solid, material)
        }
    }


    public companion object {
        private val random: Random = Random(222)

        private val colorCache = HashMap<GdmlMaterial, Int>()

        /**
         * Use random color and cache it based on the material. Meaning that colors are random, but always the same for the
         * same material.
         */
        public fun randomColor(material: GdmlMaterial): Int {
            return colorCache.getOrPut(material) { random.nextInt(16777216) }
        }
    }
}

private class GdmlTransformerEnv(val settings: GdmlTransformer) {
    //private val materialCache = HashMap<GdmlMaterial, Meta>()

    /**
     * A special group for local templates
     */
    private val proto = SolidGroup()

    private val solids = proto.group(solidsName) {
        setProperty("edges.enabled", false)
    }

    private val referenceStore = HashMap<Name, MutableList<SolidReferenceGroup>>()

    fun Solid.configureSolid(root: Gdml, parent: GdmlVolume, solid: GdmlSolid) {
        val material = parent.materialref.resolve(root) ?: GdmlElement(parent.materialref.ref)
        with(settings) {
            with(this@configureSolid) {
                configureSolid(parent, solid, material)
            }
        }
    }

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

    fun <T : Solid> T.withPosition(
        newPos: GdmlPosition? = null,
        newRotation: GdmlRotation? = null,
        newScale: GdmlScale? = null,
    ): T = apply {
        newPos?.let {
            val point = Point3D(it.x(settings.lUnit), it.y(settings.lUnit), it.z(settings.lUnit))
            if (point != Point3D.ZERO) {
                position = point
            }
        }
        newRotation?.let {
            val point = Point3D(it.x(settings.aUnit), it.y(settings.aUnit), it.z(settings.aUnit))
            if (point != Point3D.ZERO) {
                rotation = point
            }
            //this@withPosition.rotationOrder = RotationOrder.ZXY
        }
        newScale?.let {
            val point = Point3D(it.x, it.y, it.z)
            if (point != Point3D.ONE) {
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
                radius = solid.rmax * lScale,
                height = solid.z * lScale,
                innerRadius = solid.rmin * lScale,
                startAngle = solid.startphi * aScale,
                angle = solid.deltaphi * aScale,
                name = name
            )
            is GdmlCone -> if (solid.rmin1.toDouble() == 0.0 && solid.rmin2.toDouble() == 0.0) {
                cone(
                    bottomRadius = solid.rmax1 * lScale,
                    height = solid.z * lScale,
                    upperRadius = solid.rmax2 * lScale,
                    name = name
                ) {
                    startAngle = solid.startphi * aScale
                    angle = solid.deltaphi * aScale
                }
            } else {
                coneSurface(
                    bottomOuterRadius = solid.rmax1 * lScale,
                    bottomInnerRadius = solid.rmin1 * lScale,
                    height = solid.z * lScale,
                    topOuterRadius = solid.rmax2 * lScale,
                    topInnerRadius = solid.rmin2 * lScale,
                    name = name
                ) {
                    startAngle = solid.startphi * aScale
                    angle = solid.deltaphi * aScale
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
                val dxBottom = solid.x1.toDouble() / 2
                val dxTop = solid.x2.toDouble() / 2
                val dyBottom = solid.y1.toDouble() / 2
                val dyTop = solid.y2.toDouble() / 2
                val dz = solid.z.toDouble() / 2
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
            GdmlTransformer.Action.ADD -> {
                addSolid(root, solid, name)
            }
            GdmlTransformer.Action.PROTOTYPE -> {
                proxySolid(root, this, solid, name ?: solid.name)
            }
            GdmlTransformer.Action.REJECT -> {
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
                configureSolid(root, volume, solid)
                withPosition(root, physVolume)
            }
            return
        }

        when (settings.volumeAction(volume)) {
            GdmlTransformer.Action.ADD -> {
                val group: SolidGroup = volume(root, volume)
                this[physVolume.name] = group.withPosition(root, physVolume)
            }
            GdmlTransformer.Action.PROTOTYPE -> {
                proxyVolume(root, this, physVolume, volume)
            }
            GdmlTransformer.Action.REJECT -> {
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
                this.configureSolid(root, group, solid)
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
        val rootStyle by final.style("gdml") {
            Solid.ROTATION_ORDER_KEY put RotationOrder.ZXY
        }
        final.useStyle(rootStyle)

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
        settings.styleCache.forEach {
            final.styleSheet {
                define(it.key.toString(), it.value)
            }
        }
        return final
    }

    fun transform(root: Gdml): SolidGroup =
        finalize(volume(root, root.world.resolve(root) ?: error("GDML root is not resolved")))
}


public fun Gdml.toVision(block: GdmlTransformer.() -> Unit = {}): SolidGroup {
    val settings = GdmlTransformer().apply(block)
    val context = GdmlTransformerEnv(settings)
    return context.transform(this)
}

/**
 * Append Gdml node to the group
 */
public fun SolidGroup.gdml(gdml: Gdml, key: String? = null, transformer: GdmlTransformer.() -> Unit = {}) {
    val visual = gdml.toVision(transformer)
    //println(Visual3DPlugin.json.stringify(VisualGroup3D.serializer(), visual))
    set(key, visual)
}

@VisionBuilder
@DFExperimental
public inline fun VisionOutput.gdml(block: Gdml.() -> Unit): SolidGroup = Gdml(block).toVision()