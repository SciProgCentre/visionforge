package ru.mipt.npm.root

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TGeoMedium")
public class TGeoMedium(
    public val fId : Int,
    public val fMaterial: TGeoMaterial,
    public val fParams: DoubleArray
): TNamed()