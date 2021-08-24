package ru.mipt.npm.root

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TGeoNode")
public open class TGeoNode : TNamed() {
    public val fGeoAtt: UInt = 0u

    @Contextual
    public val fVolume: TGeoVolume? = null

//    @Contextual
//    public val fMother: TGeoVolume? = null

    public val fNumber: Int = 0
    public val fNovlp: Int = 0
    public val fOverlaps: IntArray = intArrayOf()
}

@Serializable
@SerialName("TGeoNodeMatrix")
public class TGeoNodeMatrix : TGeoNode() {
    @Contextual
    public val fMatrix: TGeoMatrix? = null
}