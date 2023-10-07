package ru.mipt.npm.root

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.double
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.set
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.parseAsName
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.names.withIndex
import space.kscience.kmath.complex.Quaternion
import space.kscience.kmath.geometry.fromRotationMatrix
import space.kscience.kmath.linear.VirtualMatrix
import space.kscience.visionforge.MutableVisionContainer
import space.kscience.visionforge.isEmpty
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.solid.SolidMaterial.Companion.MATERIAL_COLOR_KEY
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val volumesName = Name.EMPTY //"volumes".asName()

private operator fun Number.times(d: Double) = toDouble() * d

private operator fun Number.times(f: Float) = toFloat() * f

private fun degToRad(d: Double) = d * PI / 180.0

private data class RootToSolidContext(
    val prototypeHolder: PrototypeHolder,
    val currentLayer: Int = 0,
    val maxLayer: Int = 5,
    val ignoreRootColors: Boolean = false,
    val colorCache: MutableMap<Meta, String> = mutableMapOf(),
)

// apply rotation from a matrix
private fun Solid.rotate(rot: DoubleArray) {
    val matrix = VirtualMatrix(3, 3) { i, j -> rot[i * 3 + j] }
    quaternion = Quaternion.fromRotationMatrix(matrix)
}

private fun Solid.translate(trans: DoubleArray) {
    val (x, y, z) = trans
    position = Float32Vector3D(x, y, z)
}

private fun Solid.scale(s: DoubleArray) {
    scale = Float32Vector3D(s[0], s[1], s[2])
}

private fun Solid.useMatrix(matrix: DGeoMatrix?): Unit {
    when (matrix) {
        null -> {}
        is DGeoIdentity -> {}
        is DGeoTranslation -> translate(matrix.fTranslation)
        is DGeoRotation -> rotate(matrix.fRotationMatrix)
        is DGeoScale -> scale(matrix.fScale)
        is DGeoGenTrans -> {
            translate(matrix.fTranslation)
            matrix.fRotation?.fRotationMatrix?.let { rotate(it) }
            scale(matrix.fScale)
        }

        is DGeoCombiTrans -> {
            translate(matrix.fTranslation)
            matrix.fRotation?.fRotationMatrix?.let { rotate(it) }
        }

        is DGeoHMatrix -> {
            translate(matrix.fTranslation)
            matrix.fRotation?.fRotationMatrix?.let { rotate(it) }
            scale(matrix.fScale)
        }
    }
}

private fun SolidGroup.addShape(
    shape: DGeoShape,
    context: RootToSolidContext,
    name: String? = shape.fName.ifEmpty { null },
    block: Solid.() -> Unit = {},
) {
    when (shape.typename) {
        "TGeoCompositeShape" -> {
            val fNode: DGeoBoolNode? by shape.dObject(::DGeoBoolNode)
            val node = fNode ?: error("Composite shape node not resolved")
            val compositeType = when (node.typename) {
                "TGeoIntersection" -> CompositeType.INTERSECT
                "TGeoSubtraction" -> CompositeType.SUBTRACT
                "TGeoUnion" -> CompositeType.GROUP
                else -> error("Unknown bool node type ${node.typename}")
            }
            smartComposite(compositeType, name = name) {
                addShape(node.fLeft!!, context, null) {
                    useMatrix(node.fLeftMat)
                }
                addShape(node.fRight!!, context, null) {
                    useMatrix(node.fRightMat)
                }
            }.apply(block)
        }

        "TGeoXtru" -> {
            val fNvert by shape.meta.int(0)
            val fX by shape.meta.doubleArray()
            val fY by shape.meta.doubleArray()
            val fNz by shape.meta.int(0)
            val fZ by shape.meta.doubleArray()
            val fX0 by shape.meta.doubleArray()
            val fY0 by shape.meta.doubleArray()
            val fScale by shape.meta.doubleArray()

            extruded(name = name) {
                shape {
                    (0 until fNvert).forEach { index ->
                        point(fX[index], fY[index])
                    }
                }

                (0 until fNz).forEach { index ->
                    layer(
                        fZ[index],
                        fX0[index],
                        fY0[index],
                        fScale[index]
                    )
                }
            }.apply(block)
        }

        "TGeoTube" -> {
            val fRmax by shape.meta.double(0.0)
            val fDz by shape.meta.double(0.0)
            val fRmin by shape.meta.double(0.0)

            tube(
                radius = fRmax,
                height = fDz * 2,
                innerRadius = fRmin,
                name = name,
                block = block
            )
        }

        "TGeoTubeSeg" -> {
            val fRmax by shape.meta.double(0.0)
            val fDz by shape.meta.double(0.0)
            val fRmin by shape.meta.double(0.0)
            val fPhi1 by shape.meta.double(0.0)
            val fPhi2 by shape.meta.double(0.0)

            tube(
                radius = fRmax,
                height = fDz * 2,
                innerRadius = fRmin,
                startAngle = degToRad(fPhi1),
                angle = degToRad(fPhi2 - fPhi1),
                name = name,
                block = block
            )
        }

        "TGeoPcon" -> {
            val fDphi by shape.meta.double(0.0)
            val fNz by shape.meta.int(2)
            val fPhi1 by shape.meta.double(360.0)
            val fRmax by shape.meta.doubleArray()
            val fRmin by shape.meta.doubleArray()
            val fZ by shape.meta.doubleArray()
            if (fNz == 2) {
                coneSurface(
                    bottomOuterRadius = fRmax[0],
                    bottomInnerRadius = fRmin[0],
                    height = fZ[1] - fZ[0],
                    topOuterRadius = fRmax[1],
                    topInnerRadius = fRmin[1],
                    startAngle = degToRad(fPhi1),
                    angle = degToRad(fDphi),
                    name = name,
                ) {
                    z = (fZ[1] + fZ[0]) / 2
                }.apply(block)
            } else {
                TODO("Polycone is not implemented")
            }
        }

        "TGeoPgon" -> {

            val fDphi by shape.meta.double(0.0)
            val fNz by shape.meta.int(2)
            val fPhi1 by shape.meta.double(360.0)
            val fRmax by shape.meta.doubleArray()
            val fRmin by shape.meta.doubleArray()
            val fZ by shape.meta.doubleArray()

            val fNedges by shape.meta.int(1)

            val startphi = degToRad(fPhi1)
            val deltaphi = degToRad(fDphi)

            fun Shape2DBuilder.pGon(radius: Double){
                (0..<fNedges).forEach {
                    val phi = deltaphi / fNedges * it + startphi
                    point(radius * cos(phi), radius * sin(phi))
                }
            }

            surface(name) {
                //getting the radius of first
                require(fNz > 1) { "The polyhedron geometry requires at least two planes" }
                for (index in 0 until fNz){
                    layer(
                        fZ[index],
                        innerBuilder = {
                            pGon(fRmin[index])
                        },
                        outerBuilder = {
                            pGon(fRmax[index])
                        }
                    )
                }
            }.apply(block)
        }

        "TGeoShapeAssembly" -> {
            val fVolume by shape.dObject(::DGeoVolume)
            fVolume?.let { volume ->
                addRootVolume(volume, context, name = volume.fName.ifEmpty { null }, block = block)
            }
        }

        "TGeoBBox" -> {
            box(shape.fDX * 2, shape.fDY * 2, shape.fDZ * 2, name = name, block = block)
        }

        "TGeoTrap" -> {
            val fTheta by shape.meta.double(0.0)
            val fPhi by shape.meta.double(0.0)
            val fAlpha1 by shape.meta.double(0.0)
            val fAlpha2 by shape.meta.double(0.0)
            if (fAlpha1 != 0.0 || fAlpha2 != 0.0 || fTheta != 0.0 || fPhi != 0.0) {
                TODO("Angled trapezoid not implemented")
            }
            val fH1 by shape.meta.double(0.0)
            val fBl1 by shape.meta.double(0.0)
            val fTl1 by shape.meta.double(0.0)
            val fH2 by shape.meta.double(0.0)
            val fBl2 by shape.meta.double(0.0)
            val fTl2 by shape.meta.double(0.0)

            val fDz by shape.meta.double(0.0)
            //TODO check proper node order
            val node1 = Float32Vector3D(-fBl1, -fH1, -fDz)
            val node2 = Float32Vector3D(fBl1, -fH1, -fDz)
            val node3 = Float32Vector3D(fTl1, fH1, -fDz)
            val node4 = Float32Vector3D(-fTl1, fH1, -fDz)
            val node5 = Float32Vector3D(-fBl2, -fH2, fDz)
            val node6 = Float32Vector3D(fBl2, -fH2, fDz)
            val node7 = Float32Vector3D(fTl2, fH2, fDz)
            val node8 = Float32Vector3D(-fTl2, fH2, fDz)
            hexagon(node1, node2, node3, node4, node5, node6, node7, node8, name)
        }

        "TGeoScaledShape" -> {
            val fShape by shape.dObject(::DGeoShape)
            val fScale by shape.dObject(::DGeoScale)
            fShape?.let { scaledShape ->
                solidGroup(name?.let { Name.parse(it) }) {
                    scale = Float32Vector3D(fScale?.x ?: 1.0, fScale?.y ?: 1.0, fScale?.z ?: 1.0)
                    addShape(scaledShape, context)
                    apply(block)
                }
            }
        }

        else -> {
            TODO("A shape with type ${shape.typename} not implemented")
        }
    }
}

private fun SolidGroup.addRootNode(obj: DGeoNode, context: RootToSolidContext) {
    val volume = obj.fVolume ?: return
    addRootVolume(volume, context, obj.fName) {
        when (obj.typename) {
            "TGeoNodeMatrix" -> {
                val fMatrix by obj.dObject(::dGeoMatrix)
                this.useMatrix(fMatrix)
            }

            "TGeoNodeOffset" -> {
                val fOffset by obj.meta.double(0.0)
                x = fOffset
            }
        }
    }
}

private fun buildVolume(volume: DGeoVolume, context: RootToSolidContext): Solid? {
    val group = SolidGroup().apply {
        //set current layer
        layer = context.currentLayer
        val nodes = volume.fNodes

        if (volume.typename != "TGeoVolumeAssembly" && (nodes.isEmpty() || context.currentLayer >= context.maxLayer)) {
            //TODO add smart filter
            volume.fShape?.let { shape ->
                addShape(shape, context)
            }
        } else {
            val newLayer = if (nodes.size <= 2) {
                context.currentLayer
            } else if (nodes.size > 10) {
                context.currentLayer + 2
            } else {
                context.currentLayer + 1
            }
            val newContext = context.copy(currentLayer = newLayer)
            nodes.forEach { node ->
                //add children to the next layer
                addRootNode(node, newContext)
            }
        }
    }
    return if (group.children.isEmpty()) {
        null
    } else if (group.items.size == 1 && group.properties.own == null) {
        group.items.values.first().apply { parent = null }
    } else {
        group
    }.apply {
        volume.fMedium?.let { medium ->
            color(context.colorCache.getOrPut(medium.meta) { RootColors[11 + context.colorCache.size] })
        }

        if (!context.ignoreRootColors) {
            volume.fFillColor?.let {
                properties[MATERIAL_COLOR_KEY] = RootColors[it]
            }
        }
    }
}

//private val SolidGroup.rootPrototypes: SolidGroup get() = (parent as? SolidGroup)?.rootPrototypes ?: this

private fun SolidGroup.addRootVolume(
    volume: DGeoVolume,
    context: RootToSolidContext,
    name: String? = null,
    cache: Boolean = true,
    block: Solid.() -> Unit = {},
) {
    val combinedName = name?.parseAsName()?.let {
        // this fix is required to work around malformed root files with duplicated node names
        if (get(it) != null) {
            it.withIndex(volume.hashCode().toString(16))
        } else {
            it
        }
    }

    if (!cache) {
        val group = buildVolume(volume, context)?.apply(block) ?: return
        setChild(combinedName, group)
    } else {
        val templateName = volumesName + volume.name
        val existing = context.prototypeHolder.getPrototype(templateName)
        if (existing == null) {
            context.prototypeHolder.prototypes {
                val group = buildVolume(volume, context) ?: return@prototypes
                setChild(templateName, group)
            }
        }

        ref(templateName, combinedName).apply(block)
    }
}

public fun MutableVisionContainer<Solid>.rootGeo(
    dGeoManager: DGeoManager,
    name: String? = null,
    maxLayer: Int = 5,
    ignoreRootColors: Boolean = false,
): SolidGroup = solidGroup(name = name?.parseAsName()) {
    val context = RootToSolidContext(this, maxLayer = maxLayer, ignoreRootColors = ignoreRootColors)
    dGeoManager.fNodes.forEach { node ->
        addRootNode(node, context)
    }
}