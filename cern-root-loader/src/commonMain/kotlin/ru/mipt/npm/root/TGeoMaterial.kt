package ru.mipt.npm.root

import kotlinx.serialization.Serializable

@Serializable
public open class TGeoMaterial: TNamed()

@Serializable
public class TGeoMixture: TGeoMaterial()