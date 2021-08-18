package ru.mipt.npm.root

import kotlinx.serialization.Serializable


@Serializable
public sealed class TGeoMatrix : TNamed()

@Serializable
public class TGeoIdentity : TGeoMatrix()

@Serializable
public class TGeoHMatrix(
    public val fTranslation: DoubleArray,
    public val fRotationMatrix: DoubleArray,
    public val fScale: DoubleArray
) : TGeoMatrix()

@Serializable
public class TGeoTranslation(
    public val fTranslation: DoubleArray
) : TGeoMatrix()

@Serializable
public class TGeoRotation(
    public val fRotationMatrix: DoubleArray
): TGeoMatrix()

@Serializable
public class TGeoCombiTrans(
    public val fTranslation: DoubleArray,
    public val fRotation: TGeoRotation? = null,
): TGeoMatrix()