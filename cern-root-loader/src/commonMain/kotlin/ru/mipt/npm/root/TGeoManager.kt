package ru.mipt.npm.root

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

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
        public fun decodeFromJson(jsonObject: JsonObject): TGeoManager = TODO()

        public fun decodeFromString(string: String): TGeoManager =
            Root().decodeFromString(serializer(), string)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private class RootJsonSerializer<T>(private val tSerializer: KSerializer<T>) : KSerializer<T> {

    private val refCache: HashMap<UInt, TObject> = HashMap()


    override val descriptor: SerialDescriptor get() = tSerializer.descriptor


    override fun deserialize(decoder: Decoder): T {
        val input =  decoder as JsonDecoder
        val element = input.decodeJsonElement()
        return input.json.decodeFromJsonElement(tSerializer, transformDeserialize(element))
    }

    override fun serialize(encoder: Encoder, value: T) {
        tSerializer.serialize(encoder, value)
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        val match = refRegex.matchEntire(string)
        return if (match != null) {
            //Do unref
            val ref = match.value.toUIntOrNull() ?: error("Ref value is not a number")
            val refValue = refCache[ref] ?: error("Reference $ref unresolved")
            refValue as T //TODO research means to make it safe
        } else {
            val res = rootJson.decodeFromString(deserializer, string)
            val uid = (res as? TObject)?.fUniqueID
            if (uid != null && refCache[uid] == null) {
                refCache[uid] = res
            }
            res
        }
    }


    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String =
        rootJson.encodeToString(serializer, value)

    companion object {
        val refRegex = """\{\s*"${"\\$"}ref"\s*:\s*(\d*)}""".toRegex()

        val rootSerializersModule = SerializersModule {
            polymorphic(TGeoShape::class) {
                subclass(TGeoBBox.serializer())
                subclass(TGeoCompositeShape.serializer())
                subclass(TGeoXtru.serializer())
                subclass(TGeoTube.serializer())
                subclass(TGeoTubeSeg.serializer())
                subclass(TGeoShapeAssembly.serializer())
            }

            polymorphic(TGeoMatrix::class) {
                subclass(TGeoIdentity.serializer())
                subclass(TGeoHMatrix.serializer())
                subclass(TGeoTranslation.serializer())
                subclass(TGeoRotation.serializer())
                subclass(TGeoCombiTrans.serializer())
            }

            polymorphic(TObject::class) {
                subclass(TGeoBBox.serializer())
                subclass(TGeoCompositeShape.serializer())
                subclass(TGeoXtru.serializer())
                subclass(TGeoTube.serializer())
                subclass(TGeoTubeSeg.serializer())
                subclass(TGeoShapeAssembly.serializer())

                subclass(TGeoIdentity.serializer())
                subclass(TGeoHMatrix.serializer())
                subclass(TGeoTranslation.serializer())
                subclass(TGeoRotation.serializer())
                subclass(TGeoCombiTrans.serializer())

                subclass(TGeoMaterial.serializer())
                subclass(TGeoMixture.serializer())

                subclass(TGeoMedium.serializer())

                subclass(TGeoNode.serializer())
                subclass(TGeoNodeMatrix.serializer())
                subclass(TGeoVolume.serializer())
                subclass(TGeoVolumeAssembly.serializer())
            }
            polymorphic(TGeoNode::class, TGeoNode.serializer()) {
                subclass(TGeoNodeMatrix.serializer())
            }
            polymorphic(TGeoVolume::class, TGeoVolume.serializer()) {
                subclass(TGeoVolumeAssembly.serializer())
            }
        }

        val rootJson: Json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            classDiscriminator = "_typename"
            serializersModule = rootSerializersModule
        }

    }

}
