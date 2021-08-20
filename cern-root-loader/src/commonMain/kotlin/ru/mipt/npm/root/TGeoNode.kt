package ru.mipt.npm.root

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TGeoNode")
public open class TGeoNode : TNamed() {
    //val fGeoAtt: UInt
    public val fVolume: TGeoVolume? = null
    public val fMother: TGeoVolume? = null
    public val fNumber: Int = 0
    public val fNovlp: Int = 0
    public val fOverlaps: IntArray = intArrayOf()
}

@Serializable
@SerialName("TGeoNodeMatrix")
public class TGeoNodeMatrix : TGeoNode() {
    public val fMatrix: TGeoMatrix? = null
}