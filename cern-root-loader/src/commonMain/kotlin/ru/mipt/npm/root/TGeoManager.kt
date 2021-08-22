package ru.mipt.npm.root

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TGeoManager")
public class TGeoManager : TNamed() {

    @Contextual
    public val fMatrices: TObjArray = TObjArray.empty

    @Contextual
    public val fShapes: TObjArray = TObjArray.empty

    @Contextual
    public val fVolumes: TObjArray = TObjArray.empty
}
