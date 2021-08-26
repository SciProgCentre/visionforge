package ru.mipt.npm.root

import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.solid.*
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt


private val solidsName = "solids".asName()
private val volumesName = "volumes".asName()

private operator fun Number.times(d: Double) = toDouble() * d

private operator fun Number.times(f: Float) = toFloat() * f

private fun degToRad(d: Double) = d * PI / 180.0

// converting to XYZ to Tait–Bryan angles according to https://en.wikipedia.org/wiki/Euler_angles#Rotation_matrix
private fun Solid.rotate(rot: DoubleArray) {
    val xAngle = atan2(-rot[5], rot[8])
    val yAngle = atan2(rot[2], sqrt(1.0 - rot[2].pow(2)))
    val zAngle = atan2(-rot[1], rot[0])
    rotation = Point3D(xAngle, yAngle, zAngle)
}

private fun Solid.translate(trans: DoubleArray) {
    val (x, y, z) = trans
    position = Point3D(x, y, z)
}

private fun Solid.useMatrix(matrix: TGeoMatrix?) {
    when (matrix) {
        null, is TGeoIdentity -> {
            //do nothing
        }
        is TGeoTranslation -> {
            translate(matrix.fTranslation)
        }
        is TGeoRotation -> {
            rotate(matrix.fRotationMatrix)
        }
        is TGeoCombiTrans -> {
            translate(matrix.fTranslation)
            matrix.fRotation?.let { rotate(it.fRotationMatrix) }
        }
        is TGeoHMatrix -> {
            translate(matrix.fTranslation)
            rotate(matrix.fRotationMatrix)
            val (xScale, yScale, zScale) = matrix.fScale
            scale = Point3D(xScale, yScale, zScale)
        }
    }
}

private fun SolidGroup.addShape(shape: TGeoShape) {
    when (shape) {
        is TGeoShapeRef -> addShape(shape.value)
        is TGeoCompositeShape -> {
            val bool: TGeoBoolNode = shape.fNode
            val compositeType = when (bool) {
                is TGeoIntersection -> CompositeType.INTERSECT
                is TGeoSubtraction -> CompositeType.SUBTRACT
                is TGeoUnion -> CompositeType.UNION
            }
            composite(compositeType, name = shape.fName) {
                addShape(bool.fLeft).apply {
                    useMatrix(bool.fLeftMat)
                }
                addShape(bool.fRight).apply {
                    useMatrix(bool.fRightMat)
                }
            }
        }
        is TGeoXtru -> extruded(name = shape.fName) {

            (0 until shape.fNvert).forEach { index ->
                shape {
                    point(shape.fX[index], shape.fY[index])
                }
            }

            (0 until shape.fNz).forEach { index ->
                layer(
                    shape.fZ[index],
                    shape.fX0[index],
                    shape.fY0[index],
                    shape.fScale[index]
                )
            }
        }
        is TGeoTube -> tube(
            radius = shape.fRmax,
            height = shape.fDz * 2,
            innerRadius = shape.fRmin,
            name = shape.fName
        )
        is TGeoTubeSeg -> tube(
            radius = shape.fRmax,
            height = shape.fDz * 2,
            innerRadius = shape.fRmin,
            startAngle = degToRad(shape.fPhi1),
            angle = degToRad(shape.fPhi2 - shape.fPhi1),
            name = shape.fName
        )
        is TGeoPcon -> TODO()
        is TGeoPgon -> TODO()
        is TGeoShapeAssembly -> volume(shape.fVolume)
        is TGeoBBox -> box(shape.fDX * 2, shape.fDY * 2, shape.fDZ * 2, name = shape.fName)
    }
}

private fun SolidGroup.node(obj: TGeoNode) {
    if (obj.fVolume != null) {
        volume(obj.fVolume, obj.fName).apply {
            when (obj) {
                is TGeoNodeMatrix -> {
                    useMatrix(obj.fMatrix)
                }
                is TGeoNodeOffset -> {
                    x = obj.fOffset
                }
            }
        }
    }
}

private fun buildGroup(volume: TGeoVolume): SolidGroup {
    return if (volume is TGeoVolumeAssemblyRef) {
        buildGroup(volume.value)
    } else {
        SolidGroup {
            volume.fShape?.let { addShape(it) }
            volume.fNodes?.let {
                it.arr.forEach { obj ->
                    node(obj)
                }
            }
        }
    }
}

private val SolidGroup.rootPrototypes: SolidGroup get() = (parent as? SolidGroup)?.rootPrototypes ?: this

private fun SolidGroup.volume(volume: TGeoVolume, name: String? = null, cache: Boolean = true): Solid {
    val group = buildGroup(volume)
    val combinedName = if (volume.fName.isEmpty()) {
        name
    } else if (name == null) {
        volume.fName
    } else {
        "${name}_${volume.fName}"
    }
    return if (!cache) {
        group
    } else newRef(
        name = combinedName,
        obj = group,
        prototypeHolder = rootPrototypes,
        templateName = volumesName + Name.parse(combinedName ?: "volume[${group.hashCode()}]")
    )
}

//    private fun load(geo: TGeoManager): SolidGroup {
////        /**
////         * A special group for local templates
////         */
////        val proto = SolidGroup()
////
////        val solids = proto.group(solidsName) {
////            setPropertyNode("edges.enabled", false)
////        }
////
////        val volumes = proto.group(volumesName)
////
////        val referenceStore = HashMap<Name, MutableList<SolidReferenceGroup>>()
//    }


public fun TGeoManager.toSolid(): SolidGroup = SolidGroup {
    fNodes.arr.forEach {
        node(it)
    }
}