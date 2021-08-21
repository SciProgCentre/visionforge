package ru.mipt.npm.root

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
@SerialName("TGeoManager")
public class TGeoManager : TNamed() {

    public val fMatrices: TObjArray = TObjArray.empty
    public val fShapes: TObjArray = TObjArray.empty
    public val fVolumes: TObjArray = TObjArray.empty


    public companion object {


        /**
         * Load Json encoded TGeoManager
         */
        public fun decodeFromJson(jsonElement: JsonElement): TGeoManager =
            rootJson().decodeFromJsonElement(jsonElement)

        public fun decodeFromString(string: String): TGeoManager =
            rootJson().decodeFromString(serializer(), string)
    }
}
