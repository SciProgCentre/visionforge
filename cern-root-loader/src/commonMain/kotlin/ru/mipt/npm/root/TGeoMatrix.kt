package ru.mipt.npm.root

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("TGeoMatrix")
public sealed class TGeoMatrix : TNamed()

@Serializable
@SerialName("TGeoIdentity")
public class TGeoIdentity : TGeoMatrix()

@Serializable
@SerialName("TGeoHMatrix")
public class TGeoHMatrix(
    public val fTranslation: DoubleArray,
    public val fRotationMatrix: DoubleArray,
    public val fScale: DoubleArray
) : TGeoMatrix()

@Serializable
@SerialName("TGeoTranslation")
public class TGeoTranslation(
    public val fTranslation: DoubleArray
) : TGeoMatrix()

@Serializable
@SerialName("TGeoRotation")
public class TGeoRotation(
    public val fRotationMatrix: DoubleArray
) : TGeoMatrix()

@Serializable
@SerialName("TGeoCombiTrans")
public class TGeoCombiTrans(
    public val fTranslation: DoubleArray,
    @Contextual
    public val fRotation: TGeoRotation? = null,
) : TGeoMatrix()