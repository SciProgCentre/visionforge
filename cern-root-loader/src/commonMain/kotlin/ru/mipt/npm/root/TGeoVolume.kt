package ru.mipt.npm.root

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TGeoVolume")
public open class TGeoVolume : TNamed() {
    public val fGeoAtt: UInt = 0u
    public val fLineColor: Int = 2
    public val fLineStyle: Int? = null
    public val fLineWidth: UInt = 1u
    public val fFillColor: Int? = null
    public val fFillStyle: Int? = null

    @Contextual
    public val fNodes: TObjArray? = null

    @Contextual
    public val fShape: TGeoShape? = null

    @Contextual
    public val fMedium: TGeoMedium? = null

    public val fNumber: Int = 1
    public val fNtotal: Int = 1
    public val fRefCount: Int = 1
}

public class TGeoVolumeRef(provider: () -> TGeoVolume) : TGeoVolume() {
    public val value: TGeoVolume by lazy(provider)
}

@Serializable
@SerialName("TGeoVolumeAssembly")
public open class TGeoVolumeAssembly : TGeoVolume()

public class TGeoVolumeAssemblyRef(provider: () -> TGeoVolumeAssembly) : TGeoVolumeAssembly() {
    public val value: TGeoVolumeAssembly by lazy(provider)
}