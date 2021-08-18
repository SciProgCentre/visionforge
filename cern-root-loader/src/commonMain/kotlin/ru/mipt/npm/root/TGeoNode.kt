package ru.mipt.npm.root

import kotlinx.serialization.Serializable

@Serializable
public class TGeoNode : TNamed() {
    //val fGeoAtt: UInt
    public val fVolume: TGeoVolume? = null
    public val fMother: TGeoVolume? = null
    public val fNumber: Int = 0
    public val fNovlp: Int = 0
    public val fOverlaps: IntArray = intArrayOf()
}

public class TGeoNodeMatrix : TGeoMatrix() {
    public val fMatrix: TGeoMatrix? = null
}