package ru.mipt.npm.root

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TGeoManager")
public class TGeoManager : TNamed() {

    @Contextual
    public val fMatrices: TObjArray<@Contextual TGeoMatrix> = TObjArray.getEmpty()

    @Contextual
    public val fShapes: TObjArray<@Contextual TGeoShape> = TObjArray.getEmpty()

    @Contextual
    public val fVolumes: TObjArray<@Contextual TGeoVolume> = TObjArray.getEmpty()

    @Contextual
    public val fNodes: TObjArray<@Contextual TGeoNode> = TObjArray.getEmpty()
}
