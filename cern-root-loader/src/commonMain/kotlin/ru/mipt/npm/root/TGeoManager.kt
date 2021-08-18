package ru.mipt.npm.root

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@Serializable
public class TGeoManager : TNamed() {

    public val fMatrices: TObjArray = TObjArray.empty
    public val fShapes: TObjArray = TObjArray.empty
    public val fVolumes: TObjArray = TObjArray.empty
    

    companion object {
        public val rootJson: Json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            classDiscriminator = "_typename"
        }


        /**
         * Load Json encoded TGeoManager
         */
        public fun decodeFromJson(jsonObject: JsonObject): TGeoManager = TODO()
    }
}


