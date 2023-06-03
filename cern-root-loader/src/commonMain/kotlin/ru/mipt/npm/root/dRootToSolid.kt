package ru.mipt.npm.root

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.parseAsName
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.MutableVisionContainer
import space.kscience.visionforge.isEmpty
import space.kscience.visionforge.set
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.solid.SolidMaterial.Companion.MATERIAL_COLOR_KEY
import kotlin.math.*

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

// converting to XYZ to Tait–Bryan angles according to https://en.wikipedia.org/wiki/Euler_angles#Rotation_matrix
private fun Solid.rotate(rot: DoubleArray) {
    val xAngle = atan2(-rot[5], rot[8])
    val yAngle = atan2(rot[2], sqrt(1.0 - rot[2].pow(2)))
    val zAngle = atan2(-rot[1], rot[0])
    rotation = Float32Vector3D(xAngle, yAngle, zAngle)
}

private fun Solid.translate(trans: DoubleArray) {
    val (x, y, z) = trans
    position = Float32Vector3D(x, y, z)
}

private fun Solid.useMatrix(matrix: DGeoMatrix?) {
    if (matrix == null) return
    when (matrix.typename) {
        "TGeoIdentity" -> {
            //do nothing
        }

        "TGeoTranslation" -> {
            val fTranslation by matrix.meta.doubleArray()
            translate(fTranslation)
        }

        "TGeoRotation" -> {
            val fRotationMatrix by matrix.meta.doubleArray()
            rotate(fRotationMatrix)
        }

        "TGeoCombiTrans" -> {
            val fTranslation by matrix.meta.doubleArray()

            translate(fTranslation)
            matrix.meta["fRotation.fRotationMatrix"]?.value?.let {
                rotate(it.doubleArray)
            }
        }

        "TGeoHMatrix" -> {
            val fTranslation by matrix.meta.doubleArray()
            val fRotationMatrix by matrix.meta.doubleArray()
            val fScale by matrix.meta.doubleArray()
            translate(fTranslation)
            rotate(fRotationMatrix)
            scale = Float32Vector3D(fScale[0], fScale[1], fScale[2])
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
                    this.useMatrix(node.fLeftMat)
                }
                addShape(node.fRight!!, context, null) {
                    this.useMatrix(node.fRightMat)
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
                (0 until fNvert).forEach { index ->
                    shape {
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
                TODO()
            }
        }

        "TGeoPgon" -> {
            //TODO add a inner polygone layer
            val fDphi by shape.meta.double(0.0)
            val fNz by shape.meta.int(2)
            val fPhi1 by shape.meta.double(360.0)
            val fRmax by shape.meta.doubleArray()
            val fRmin by shape.meta.doubleArray()
            val fZ by shape.meta.doubleArray()

            val fNedges by shape.meta.int(1)

            val startphi = degToRad(fPhi1)
            val deltaphi = degToRad(fDphi)

            extruded(name) {
                //getting the radius of first
                require(fNz > 1) { "The polyhedron geometry requires at least two planes" }
                val baseRadius = fRmax[0]
                shape {
                    (0..fNedges).forEach {
                        val phi = deltaphi * fNedges * it + startphi
                        (baseRadius * cos(phi) to baseRadius * sin(phi))
                    }
                }
                (0 until fNz).forEach { index ->
                    //scaling all radii relative to first layer radius
                    layer(fZ[index], scale = fRmax[index] / baseRadius)
                }
            }.apply(block)
        }

        "TGeoShapeAssembly" -> {
            val fVolume by shape.dObject(::DGeoVolume)
            fVolume?.let { volume ->
                addRootVolume(volume, context, block = block)
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
                val fMatrix by obj.dObject(::DGeoMatrix)
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
            color.set(context.colorCache.getOrPut(medium.meta) { RootColors[11 + context.colorCache.size] })
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

    val combinedName = if (volume.fName.isEmpty()) {
        name
    } else if (name == null) {
        volume.fName
    } else {
        "${name}_${volume.fName}"
    }

    if (!cache) {
        val group = buildVolume(volume, context)?.apply(block)
        setChild(combinedName?.let { Name.parse(it) }, group)
    } else {
        val templateName = volumesName + volume.name
        val existing = getPrototype(templateName)
        if (existing == null) {
            context.prototypeHolder.prototypes {
                val group = buildVolume(volume, context)
                setChild(templateName, group)
            }
        }

        ref(templateName, name).apply(block)
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