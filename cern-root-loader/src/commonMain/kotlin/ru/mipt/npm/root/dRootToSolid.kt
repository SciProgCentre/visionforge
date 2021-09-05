package ru.mipt.npm.root

import space.kscience.dataforge.meta.double
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.solid.*
import kotlin.math.*

private val volumesName = Name.EMPTY //"volumes".asName()

private operator fun Number.times(d: Double) = toDouble() * d

private operator fun Number.times(f: Float) = toFloat() * f

private fun degToRad(d: Double) = d * PI / 180.0

private class RootToSolidContext(val prototypeHolder: PrototypeHolder)

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
            if (matrix.meta["fRotationMatrix"] != null) {
                val fRotationMatrix by matrix.meta.doubleArray()
                rotate(fRotationMatrix)
            }
        }
        "TGeoHMatrix" -> {
            val fTranslation by matrix.meta.doubleArray()
            val fRotationMatrix by matrix.meta.doubleArray()
            val fScale by matrix.meta.doubleArray()
            translate(fTranslation)
            rotate(fRotationMatrix)
            scale = Point3D(fScale[0], fScale[1], fScale[2])
        }
    }
}

private fun SolidGroup.addShape(
    shape: DGeoShape,
    context: RootToSolidContext,
    name: String? = shape.fName.ifEmpty { null }
): Solid? = when (shape.typename) {
    "TGeoCompositeShape" -> {
        val fNode: DGeoBoolNode? by shape.dObject(::DGeoBoolNode)
        val node = fNode ?: error("Composite shape node not resolved")
        val compositeType = when (node.typename) {
            "TGeoIntersection" -> CompositeType.INTERSECT
            "TGeoSubtraction" -> CompositeType.SUBTRACT
            "TGeoUnion" -> CompositeType.UNION
            else -> error("Unknown bool node type ${node.typename}")
        }
        composite(compositeType, name = name) {
            addShape(node.fLeft!!, context, "left").also {
                if (it == null) TODO()
                it.useMatrix(node.fLeftMat)
            }
            addShape(node.fRight!!, context, "right").also {
                if (it == null) TODO()
                it.useMatrix(node.fRightMat)
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
            name = name
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
            name = name
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
                name = name
            ) {
                z = (fZ[1] + fZ[0]) / 2
            }
        } else {
            TODO()
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
        }
    }
    "TGeoShapeAssembly" -> {
        val fVolume by shape.dObject(::DGeoVolume)
        fVolume?.let { volume ->
            addRootVolume(volume, context)
        }
    }
    "TGeoBBox" -> {
        box(shape.fDX * 2, shape.fDY * 2, shape.fDZ * 2, name = name)
    }
    else -> {
        TODO("A shape with type ${shape.typename} not implemented")
    }
}

private fun SolidGroup.addRootNode(obj: DGeoNode, context: RootToSolidContext) {
    val volume = obj.fVolume ?: return
    addRootVolume(volume, context, obj.fName).apply {
        when (obj.typename) {
            "TGeoNodeMatrix" -> {
                val fMatrix by obj.dObject(::DGeoMatrix)
                useMatrix(fMatrix)
            }
            "TGeoNodeOffset" -> {
                val fOffset by obj.meta.double(0.0)
                x = fOffset
            }
        }
    }
}

private fun buildGroup(volume: DGeoVolume, context: RootToSolidContext): SolidGroup = SolidGroup {
    volume.fShape?.let {
        addShape(it, context)
    }
    volume.fNodes.let {
        it.forEach { node ->
            addRootNode(node, context)
        }
    }
}

//private val SolidGroup.rootPrototypes: SolidGroup get() = (parent as? SolidGroup)?.rootPrototypes ?: this

private fun SolidGroup.addRootVolume(
    volume: DGeoVolume,
    context: RootToSolidContext,
    name: String? = null,
    cache: Boolean = true
): Solid {
    val combinedName = if (volume.fName.isEmpty()) {
        name
    } else if (name == null) {
        volume.fName
    } else {
        "${name}_${volume.fName}"
    }

    return if (!cache) {
        val group = buildGroup(volume, context)
        set(combinedName?.let { Name.parse(it) }, group)
        group
    } else {
        val templateName = volumesName + volume.name
        val existing = getPrototype(templateName)
        if (existing == null) {
            context.prototypeHolder.prototypes {
                set(templateName, buildGroup(volume, context))
            }
        }

        return ref(templateName, name)
    }
}

public fun DGeoManager.toSolid(): SolidGroup = SolidGroup {
    val context = RootToSolidContext(this)
    fNodes.forEach { node ->
        addRootNode(node, context)
    }
}