package ru.mipt.npm.root

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TGeoVolume")
public open class TGeoVolume : TNamed(){
    //    "fGeoAtt" : 3084,
//    "fLineColor" : 3,
//    "fLineStyle" : 1,
//    "fLineWidth" : 1,
//    "fFillColor" : 19,
//    "fFillStyle" : 1001,
    public lateinit var fNodes: TObjArray
    public lateinit var fShape: TGeoShape
    public lateinit var fMedium: TGeoMedium
    public val fNumber: Int = 1
    public val fNtotal: Int = 1
    public val fRefCount: Int = 1
}

@Serializable
@SerialName("TGeoVolumeAssembly")
public class TGeoVolumeAssembly : TGeoVolume()