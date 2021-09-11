package ru.mipt.npm.root.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TGeoManager")
public class TGeoManager : TNamed() {

    public val fMatrices: TObjArray<TGeoMatrix> = TObjArray.getEmpty()

    public val fShapes: TObjArray<TGeoShape> = TObjArray.getEmpty()

    public val fVolumes: TObjArray<TGeoVolume> = TObjArray.getEmpty()

    public val fNodes: TObjArray<TGeoNode> = TObjArray.getEmpty()
}
