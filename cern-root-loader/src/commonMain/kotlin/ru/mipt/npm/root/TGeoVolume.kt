package ru.mipt.npm.root

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TGeoVolume")
public open class TGeoVolume : TNamed() {
    //    "fGeoAtt" : 3084,
//    "fLineColor" : 3,
//    "fLineStyle" : 1,
//    "fLineWidth" : 1,
//    "fFillColor" : 19,
//    "fFillStyle" : 1001,
    @Contextual
    public lateinit var fNodes: TObjArray
        internal set

    @Contextual
    public lateinit var fShape: TGeoShape
        internal set

    @Contextual
    public lateinit var fMedium: TGeoMedium
        internal set

    public val fNumber: Int = 1
    public val fNtotal: Int = 1
    public val fRefCount: Int = 1
}

@Serializable
@SerialName("TGeoVolumeAssembly")
public class TGeoVolumeAssembly : TGeoVolume()