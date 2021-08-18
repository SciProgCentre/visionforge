package ru.mipt.npm.root

import kotlinx.serialization.Serializable

@Serializable
public class TGeoMedium(
    public val fId : Int,
    public val fMaterial: TGeoMaterial,
    public val fParams: DoubleArray
): TNamed()