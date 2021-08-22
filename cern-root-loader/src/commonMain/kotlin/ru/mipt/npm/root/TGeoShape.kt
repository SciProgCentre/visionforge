package ru.mipt.npm.root

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TGeoShape")
public sealed class TGeoShape : TNamed() {
    public val fShapeBits: UInt = 0u
    public val fShapeId: Int = 0
}

@Serializable
@SerialName("TGeoBBox")
public open class TGeoBBox : TGeoShape() {
    public val fDX: Double = 0.0
    public val fDY: Double = 0.0
    public val fDZ: Double = 0.0
    public val fOrigin: DoubleArray = doubleArrayOf(0.0, 0.0, 0.0)
}

@Serializable
@SerialName("TGeoBoolNode")
public sealed class TGeoBoolNode : TObject() {
    @Contextual
    public abstract val fLeft: TGeoShape

    @Contextual
    public abstract val fLeftMat: TGeoMatrix

    @Contextual
    public abstract val fRight: TGeoShape

    @Contextual
    public abstract val fRightMat: TGeoMatrix
}

@Serializable
@SerialName("TGeoUnion")
public class TGeoUnion(
    @Contextual
    override val fLeft: TGeoShape,
    @Contextual
    override val fLeftMat: TGeoMatrix,
    @Contextual
    override val fRight: TGeoShape,
    @Contextual
    override val fRightMat: TGeoMatrix
) : TGeoBoolNode()

@Serializable
@SerialName("TGeoSubtraction")
public class TGeoSubtraction(
    @Contextual
    override val fLeft: TGeoShape,
    @Contextual
    override val fLeftMat: TGeoMatrix,
    @Contextual
    override val fRight: TGeoShape,
    @Contextual
    override val fRightMat: TGeoMatrix
) : TGeoBoolNode()

@Serializable
@SerialName("TGeoIntersection")
public class TGeoIntersection(
    @Contextual
    override val fLeft: TGeoShape,
    @Contextual
    override val fLeftMat: TGeoMatrix,
    @Contextual
    override val fRight: TGeoShape,
    @Contextual
    override val fRightMat: TGeoMatrix
) : TGeoBoolNode()


@Serializable
@SerialName("TGeoCompositeShape")
public class TGeoCompositeShape(public val fNode: TGeoBoolNode) : TGeoBBox()

@Serializable
@SerialName("TGeoXtru")
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
@SerialName("TGeoTube")
public open class TGeoTube(
    public val fRmin: Double,
    public val fRmax: Double,
    public val fDz: Double,
) : TGeoBBox()

@Serializable
@SerialName("TGeoTubeSeg")
public class TGeoTubeSeg(
    public val fRmin: Double,
    public val fRmax: Double,
    public val fDz: Double,
    public val fPhi1: Double,
    public val fPhi2: Double,
    public val fS1: Double,
    public val fC1: Double,
    public val fS2: Double,
    public val fC2: Double,
    public val fSm: Double,
    public val fCm: Double,
    public val fCdfi: Double,
) : TGeoBBox()

@Serializable
@SerialName("TGeoShapeAssembly")
public class TGeoShapeAssembly(
    @Contextual
    public val fVolume: TGeoVolumeAssembly,
    public val fBBoxOK: Boolean = true
) : TGeoBBox()