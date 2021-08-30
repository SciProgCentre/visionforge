package ru.mipt.npm.root

import space.kscience.dataforge.meta.*
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

private fun Solid.useMatrix(matrix: TGeoMatrixScheme?) {
    if (matrix == null) return
    when (matrix.typename) {
        "TGeoIdentity" -> {
            //do nothing
        }
        "TGeoTranslation" -> {
            val fTranslation by matrix.doubleArray()
            translate(fTranslation)
        }
        "TGeoRotation" -> {
            val fRotationMatrix by matrix.doubleArray()
            rotate(fRotationMatrix)
        }
        "TGeoCombiTrans" -> {
            val fTranslation by matrix.doubleArray()

            translate(fTranslation)
            if (matrix.meta["fRotationMatrix"] != null) {
                val fRotationMatrix by matrix.doubleArray()
                rotate(fRotationMatrix)
            }
        }
        "TGeoHMatrix" -> {
            val fTranslation by matrix.doubleArray()
            val fRotationMatrix by matrix.doubleArray()
            val fScale by matrix.doubleArray()
            translate(fTranslation)
            rotate(fRotationMatrix)
            scale = Point3D(fScale[0], fScale[1], fScale[2])
        }
    }
}

private fun SolidGroup.addShape(shape: TGeoShapeScheme, refCache: RefCache) {
    when (shape.typename) {
        "TGeoCompositeShape" -> {
            val bool by shape.spec(TGeoBoolNodeScheme)
            val compositeType = when (bool.typename) {
                "TGeoIntersection" -> CompositeType.INTERSECT
                "TGeoSubtraction" -> CompositeType.SUBTRACT
                "TGeoUnion" -> CompositeType.UNION
                else -> error("Unknown bool node type ${bool.typename}")
            }
            composite(compositeType, name = shape.fName) {
                addShape(bool.fLeft.resolve(refCache)!!, refCache).apply {
                    useMatrix(bool.fLeftMat.resolve(refCache))
                }
                addShape(bool.fRight.resolve(refCache)!!, refCache).apply {
                    useMatrix(bool.fRightMat.resolve(refCache))
                }
            }
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

            extruded(name = shape.fName) {
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
            }
        }
        "TGeoTube" -> {
            val fRmax by shape.meta.double(0.0)
            val fDz by shape.meta.double(0.0)
            val fRmin by shape.meta.double(0.0)

            tube(
                radius = fRmax,
                height = fDz * 2,
                innerRadius = fRmin,
                name = shape.fName
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
                name = shape.fName
            )
        }
        "TGeoPcon" -> {
            TODO()
        }
        "TGeoPgon" -> {
            TODO()
        }
        "TGeoShapeAssembly" -> {
            val fVolume by shape.refSpec(TGeoVolumeScheme)
            volume(fVolume.resolve(refCache)!!, refCache)
        }
        "TGeoBBox" -> {
            box(shape.fDX * 2, shape.fDY * 2, shape.fDZ * 2, name = shape.fName)
        }
    }
}

private fun SolidGroup.node(obj: TGeoNodeScheme, refCache: RefCache) {
    val volume = obj.fVolume.resolve(refCache)
    if (volume != null) {
        volume(volume, refCache, obj.fName).apply {
            when (obj.typename) {
                "TGeoNodeMatrix" -> {
                    val fMatrix by obj.refSpec(TGeoMatrixScheme)
                    useMatrix(fMatrix.resolve(refCache))
                }
                "TGeoNodeOffset" -> {
                    val fOffset by obj.meta.double(0.0)
                    x = fOffset
                }
            }
        }
    }
}

private fun buildGroup(volume: TGeoVolumeScheme, refCache: RefCache): SolidGroup = SolidGroup {
    volume.fShape.resolve(refCache)?.let { addShape(it, refCache) }
    volume.fNodes.let {
        it.forEach { obj ->
            node(obj.resolve(refCache)!!, refCache)
        }
    }
}

private val SolidGroup.rootPrototypes: SolidGroup get() = (parent as? SolidGroup)?.rootPrototypes ?: this

private fun SolidGroup.volume(
    volume: TGeoVolumeScheme,
    refCache: RefCache,
    name: String? = null,
    cache: Boolean = true
): Solid {
    val group = buildGroup(volume, refCache)
    val combinedName = if (volume.fName.isEmpty()) {
        name
    } else if (name == null) {
        volume.fName
    } else {
        "${name}_${volume.fName}"
    }
    return if (!cache) {
        set(combinedName?.let { Name.parse(it)},group)
        group
    } else newRef(
        name = combinedName,
        obj = group,
        prototypeHolder = rootPrototypes,
        templateName = volumesName + Name.parse(combinedName ?: "volume[${group.hashCode()}]")
    )
}

public fun TGeoManagerScheme.toSolid(): SolidGroup = SolidGroup {
    fNodes.forEach {
        node(it.resolve(refCache)!!, refCache)
    }
}