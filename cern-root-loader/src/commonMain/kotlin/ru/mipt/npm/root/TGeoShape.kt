package ru.mipt.npm.root

import kotlinx.serialization.Serializable

@Serializable
public abstract class TGeoShape : TNamed() {
    public val fShapeBits: UInt = 0u
    public val fShapeId: Int = 0
}

@Serializable
public open class TGeoBBox : TGeoShape() {
    public val fDX: Double = 0.0
    public val fDY: Double = 0.0
    public val fDZ: Double = 0.0
    public val fOrigin: DoubleArray = doubleArrayOf(0.0, 0.0, 0.0)
}

@Serializable
public sealed class TGeoBoolNode : TObject() {
    public abstract val fLeft: TGeoShape
    public abstract val fLeftMat: TGeoMatrix
    public abstract val fRight: TGeoShape
    public abstract val fRightMat: TGeoMatrix
}

@Serializable
public class TGeoUnion(
    override val fLeft: TGeoShape,
    override val fLeftMat: TGeoMatrix,
    override val fRight: TGeoShape,
    override val fRightMat: TGeoMatrix
) : TGeoBoolNode()

@Serializable
public class TGeoSubtraction(
    override val fLeft: TGeoShape,
    override val fLeftMat: TGeoMatrix,
    override val fRight: TGeoShape,
    override val fRightMat: TGeoMatrix
) : TGeoBoolNode()

@Serializable
public class TGeoIntersection(
    override val fLeft: TGeoShape,
    override val fLeftMat: TGeoMatrix,
    override val fRight: TGeoShape,
    override val fRightMat: TGeoMatrix
) : TGeoBoolNode()


@Serializable
public class TGeoCompositeShape(public val fNode: TGeoBoolNode) : TGeoBBox()

@Serializable
public class TGeoXtru(
    public val fNvert: Int,
    public val fNz: Int,
    public val fZcurrent: Double,
    public val fX: DoubleArray,
    public val fY: DoubleArray,
    public val fZ: DoubleArray,
    public val fScale: DoubleArray,
    public val fX0: DoubleArray,
    public val fY0: DoubleArray
) : TGeoBBox()


@Serializable
public class TGeoTube(
    public val fRmin: Double,
    public val fRmax: Double,
    public val fDz: Double,
) : TGeoBBox()

@Serializable
public class TGeoShapeAssembly(
    public val fVolume: TGeoVolumeAssembly,
    public val fBBoxOK: Boolean = true
): TGeoBBox()