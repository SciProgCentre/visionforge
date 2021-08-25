package ru.mipt.npm.root

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.PI

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
    public val fLeftMat: TGeoMatrix? = null

    @Contextual
    public abstract val fRight: TGeoShape

    @Contextual
    public val fRightMat: TGeoMatrix? = null
}

@Serializable
@SerialName("TGeoUnion")
public class TGeoUnion(
    @Contextual
    override val fLeft: TGeoShape,
    @Contextual
    override val fRight: TGeoShape,
) : TGeoBoolNode()

@Serializable
@SerialName("TGeoSubtraction")
public class TGeoSubtraction(
    @Contextual
    override val fLeft: TGeoShape,
    @Contextual
    override val fRight: TGeoShape,
) : TGeoBoolNode()

@Serializable
@SerialName("TGeoIntersection")
public class TGeoIntersection(
    @Contextual
    override val fLeft: TGeoShape,
    @Contextual
    override val fRight: TGeoShape,
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
public open class TGeoTube : TGeoBBox() {
    public val fRmin: Double = 0.0
    public val fRmax: Double = 0.0
    public val fDz: Double = 0.0
}

@Serializable
@SerialName("TGeoTubeSeg")
public class TGeoTubeSeg(
    public val fPhi1: Double,
    public val fPhi2: Double,
    public val fS1: Double,
    public val fC1: Double,
    public val fS2: Double,
    public val fC2: Double,
    public val fSm: Double,
    public val fCm: Double,
    public val fCdfi: Double,
) : TGeoTube()

@Serializable
@SerialName("TGeoPcon")
public open class TGeoPcon : TGeoBBox() {
    public val fNz: Int = 0                         // number of z planes (at least two)
    public val fPhi1: Double = 0.0                  // lower phi limit (converted to [0,2*pi)
    public val fDphi: Double = PI * 2               // phi range
    public val fRmin: DoubleArray = doubleArrayOf() //[fNz] pointer to array of inner radii
    public val fRmax: DoubleArray = doubleArrayOf() //[fNz] pointer to array of outer radii
    public val fZ: DoubleArray = doubleArrayOf()    //[fNz] pointer to array of Z planes positions
}

@Serializable
@SerialName("TGeoPgon")
public open class TGeoPgon : TGeoPcon() {
    public val fNedges: Int = 0
}

@Serializable
@SerialName("TGeoShapeAssembly")
public class TGeoShapeAssembly(
    @Contextual
    public val fVolume: TGeoVolumeAssembly,
    public val fBBoxOK: Boolean = true
) : TGeoBBox()

public class TGeoShapeRef(provider: () -> TGeoShape) : TGeoShape() {
    public val value: TGeoShape by lazy(provider)
}