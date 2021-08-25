package ru.mipt.npm.root

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TGeoManager")
public class TGeoManager : TNamed() {

    @Contextual
    public val fMatrices: TObjArray<TGeoMatrix> = TObjArray.getEmpty()

    @Contextual
    public val fShapes: TObjArray<TGeoShape> = TObjArray.getEmpty()

    @Contextual
    public val fVolumes: TObjArray<TGeoVolume> = TObjArray.getEmpty()

    @Contextual
    public val fNodes: TObjArray<TGeoNode> = TObjArray.getEmpty()
}
