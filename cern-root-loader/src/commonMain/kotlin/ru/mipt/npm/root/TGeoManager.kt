package ru.mipt.npm.root

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule

@Serializable
public class TGeoManager : TNamed() {

    public val fMatrices: TObjArray = TObjArray.empty
    public val fShapes: TObjArray = TObjArray.empty
    public val fVolumes: TObjArray = TObjArray.empty


    public companion object {


        /**
         * Load Json encoded TGeoManager
         */
        public fun decodeFromJson(jsonObject: JsonObject): TGeoManager = TODO()
        
        public fun decodeFromString(string: String): TGeoManager =
            RootJsonSerialFormat().decodeFromString(serializer(), string)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private class RootJsonSerialFormat : StringFormat {

    override val serializersModule: SerializersModule get() = json.serializersModule

    private val refCache: HashMap<UInt, TObject> = HashMap()

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        val match = refRegex.matchEntire(string)
        return if (match != null) {
            //Do unref
            val ref = match.value.toUIntOrNull() ?: error("Ref value is not a number")
            val refValue = refCache[ref] ?: error("Reference $ref unresolved")
            refValue as T //TODO research means to make it safe
        } else {
            val res = json.decodeFromString(deserializer, string)
            val uid = (res as? TObject)?.fUniqueID
            if (uid != null && refCache[uid] == null) {
                refCache[uid] = res
            }
            res
        }
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String =
        json.encodeToString(serializer, value)

    companion object {
        val refRegex = """\{\s*"${"\\$"}ref"\s*:\s*(\d*)}""".toRegex()

        val json: Json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            classDiscriminator = "_typename"
        }
    }

}
