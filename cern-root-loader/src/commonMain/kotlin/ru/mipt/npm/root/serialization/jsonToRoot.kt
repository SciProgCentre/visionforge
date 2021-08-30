package ru.mipt.npm.root.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


private fun <T> jsonRootDeserializer(
    tSerializer: KSerializer<T>,
    builder: (JsonElement) -> T
): DeserializationStrategy<T> = object :
    DeserializationStrategy<T> {
    private val jsonElementSerializer = JsonElement.serializer()

    override val descriptor: SerialDescriptor
        get() = jsonElementSerializer.descriptor

    override fun deserialize(decoder: Decoder): T {
        val json = decoder.decodeSerializableValue(jsonElementSerializer)
        return builder(json)
    }
}

/**
 * Load Json encoded TObject
 */
public fun <T : TObject> TObject.decodeFromJson(serializer: KSerializer<T>, jsonElement: JsonElement): T =
    RootDecoder.decode(serializer, jsonElement)

public fun <T : TObject> TObject.decodeFromString(serializer: KSerializer<T>, string: String): T {
    val json = Json.parseToJsonElement(string)
    return RootDecoder.decode(serializer, json)
}

private object RootDecoder {

    private class RootUnrefSerializer<T>(
        private val tSerializer: KSerializer<T>,
        private val refCache: List<RefEntry>,
    ) : KSerializer<T> by tSerializer {

        override fun deserialize(decoder: Decoder): T {
            val input = decoder as JsonDecoder
            val element = input.decodeJsonElement()
            val refId = (element as? JsonObject)?.get("\$ref")?.jsonPrimitive?.int
            val ref = if (refId != null) {
                println("Substituting ${tSerializer.descriptor.serialName} ref $refId")
                //Forward ref for shapes
                when (tSerializer.descriptor.serialName) {
                    "TGeoShape" -> return TGeoShapeRef {
                        refCache[refId].getOrPutValue {
                            input.json.decodeFromJsonElement(tSerializer, it) as TGeoShape
                        }
                    } as T

                    "TGeoVolumeAssembly" -> return TGeoVolumeAssemblyRef {
                        refCache[refId].getOrPutValue {
                            input.json.decodeFromJsonElement(tSerializer, it) as TGeoVolumeAssembly
                        }
                    } as T

                    "TGeoVolume" -> return TGeoVolumeRef {
                        refCache[refId].getOrPutValue {
                            input.json.decodeFromJsonElement(tSerializer, it) as TGeoVolume
                        }
                    } as T

                    //Do unref
                    else -> refCache[refId]
                }
            } else {
                refCache.find { it.element == element } ?: error("Element '$element' not found in the cache")
            }

            return ref.getOrPutValue {
//                println("Decoding $it")
                val actualTypeName = it.jsonObject["_typename"]?.jsonPrimitive?.content
                input.json.decodeFromJsonElement(tSerializer, it)
            }
        }
    }

    private fun <T> KSerializer<T>.unref(refCache: List<RefEntry>): KSerializer<T> = RootUnrefSerializer(this, refCache)

    @OptIn(ExperimentalSerializationApi::class)
    fun unrefSerializersModule(
        refCache: List<RefEntry>
    ): SerializersModule = SerializersModule {

        contextual(TObjArray::class) {
            TObjArray.serializer(it[0]).unref(refCache)
        }

        contextual(TGeoMedium.serializer().unref(refCache))

        polymorphic(TGeoBoolNode::class) {
            subclass(TGeoIntersection.serializer().unref(refCache))
            subclass(TGeoUnion.serializer().unref(refCache))
            subclass(TGeoSubtraction.serializer().unref(refCache))
        }

        polymorphic(TGeoShape::class) {
            subclass(TGeoBBox.serializer())
            subclass(TGeoXtru.serializer())
            subclass(TGeoTube.serializer())
            subclass(TGeoTubeSeg.serializer())
            subclass(TGeoPcon.serializer())
            subclass(TGeoPgon.serializer())

            subclass(TGeoCompositeShape.serializer().unref(refCache))
            subclass(TGeoShapeAssembly.serializer().unref(refCache))

            default {
                if (it == null) {
                    TGeoShape.serializer().unref(refCache)
                } else {
                    error("Unrecognized shape $it")
                }
            }
        }

        polymorphic(TGeoMatrix::class) {
            subclass(TGeoIdentity.serializer())
            subclass(TGeoHMatrix.serializer().unref(refCache))
            subclass(TGeoTranslation.serializer())
            subclass(TGeoRotation.serializer())
            subclass(TGeoCombiTrans.serializer().unref(refCache))


            val unrefed = TGeoMatrix.serializer().unref(refCache)
            default {
                if (it == null) {
                    unrefed
                } else {
                    error("Unrecognized matrix $it")
                }
            }
        }

        polymorphic(TGeoVolume::class, TGeoVolume.serializer().unref(refCache)) {
            subclass(TGeoVolumeAssembly.serializer().unref(refCache))

            val unrefed = TGeoVolume.serializer().unref(refCache)
            default {
                if (it == null) {
                    unrefed
                } else {
                    error("Unrecognized volume $it")
                }
            }
        }

        polymorphic(TGeoNode::class, TGeoNode.serializer().unref(refCache)) {
            subclass(TGeoNodeMatrix.serializer().unref(refCache))
            subclass(TGeoNodeOffset.serializer().unref(refCache))

            val unrefed = TGeoNode.serializer().unref(refCache)
            default {
                if (it == null) {
                    unrefed
                } else {
                    error("Unrecognized node $it")
                }
            }
        }
    }

    /**
     * Create an instance of Json with unfolding Root references. This instance could not be reused because of the cache.
     */
    private fun unrefJson(refCache: MutableList<RefEntry>): Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "_typename"
        serializersModule = unrefSerializersModule(refCache)
    }


    fun <T : TObject> decode(sourceDeserializer: KSerializer<T>, source: JsonElement): T {
        val refCache = ArrayList<RefEntry>()

        fun fillCache(element: JsonElement) {
            when (element) {
                is JsonObject -> {
                    if (element["_typename"] != null) {
                        refCache.add(RefEntry(element))
                    }
                    element.values.forEach {
                        fillCache(it)
                    }
                }
                is JsonArray -> {
                    element.forEach {
                        fillCache(it)
                    }
                }
                else -> {
                    //ignore primitives
                }
            }
        }
        fillCache(source)

        return unrefJson(refCache).decodeFromJsonElement(sourceDeserializer.unref(refCache), source)
    }

    class RefEntry(val element: JsonElement) {

        var value: Any? = null

        fun <T> getOrPutValue(builder: (JsonElement) -> T): T {
            if (value == null) {
                value = builder(element)
            }
            return value as T
        }

        override fun toString(): String = element.toString()
    }

//    val json = Json {
//        encodeDefaults = true
//        ignoreUnknownKeys = true
//        classDiscriminator = "_typename"
//        serializersModule = this@RootDecoder.serializersModule
//    }

}