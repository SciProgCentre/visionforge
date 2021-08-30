package ru.mipt.npm.root.serialization

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TGeoMedium")
public class TGeoMedium(
    public val fId: Int,
    @Contextual
    public val fMaterial: TGeoMaterial,
    public val fParams: DoubleArray
) : TNamed()