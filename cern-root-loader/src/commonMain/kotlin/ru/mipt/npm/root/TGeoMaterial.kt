package ru.mipt.npm.root

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TGeoMaterial")
public open class TGeoMaterial: TNamed()

@Serializable
@SerialName("TGeoMixture")
public class TGeoMixture: TGeoMaterial()