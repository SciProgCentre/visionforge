package ru.mipt.npm.root

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*


private fun <T> jsonRootDeserializer(tSerializer: KSerializer<T>, builder: (JsonElement) -> T): DeserializationStrategy<T> = object :
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
public fun <T: TObject> TObject.Companion.decodeFromJson(serializer: KSerializer<T>, jsonElement: JsonElement): T =
    RootDecoder.decode(serializer, jsonElement)

public fun <T: TObject> TObject.Companion.decodeFromString(serializer: KSerializer<T>, string: String): T {
    val json = RootDecoder.json.parseToJsonElement(string)
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
                //println("Substituting ${tSerializer.descriptor.serialName} ref $refId")
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
                    //Do unref
                    else -> refCache[refId]
                }
            } else {
                refCache.find { it.element == element } ?: error("Element '$element' not found in the cache")
            }

            return ref.getOrPutValue {
//                val actualTypeName = it.jsonObject["_typename"]?.jsonPrimitive?.content
                input.json.decodeFromJsonElement(tSerializer, it)
            }
        }
    }

    private fun <T> KSerializer<T>.unref(refCache: List<RefEntry>): KSerializer<T> =
        RootUnrefSerializer(this, refCache)

    @OptIn(ExperimentalSerializationApi::class)
    fun unrefSerializersModule(
        refCache: List<RefEntry>
    ): SerializersModule = SerializersModule {
        include(serializersModule)

        //contextual(TGeoManager.serializer().unref(refCache))
        contextual(TObjArray::class) { TObjArray.serializer(it[0]).unref(refCache) }
        //contextual(TGeoVolumeAssembly.serializer().unref(refCache))
        //contextual(TGeoShapeAssembly.serializer().unref(refCache))
        contextual(TGeoRotation.serializer().unref(refCache))
        contextual(TGeoMedium.serializer().unref(refCache))
        //contextual(TGeoVolume.serializer().unref(refCache))
        //contextual(TGeoMatrix.serializer().unref(refCache))
        //contextual(TGeoNode.serializer().unref(refCache))
        //contextual(TGeoNodeOffset.serializer().unref(refCache))
        //contextual(TGeoNodeMatrix.serializer().unref(refCache))
        //contextual(TGeoShape.serializer().unref(refCache))
        //contextual(TObject.serializer().unref(refCache))


        polymorphicDefault(TGeoShape::class) {
            if (it == null) {
                TGeoShape.serializer().unref(refCache)
            } else {
                error("Unrecognized shape $it")
            }
        }

        polymorphicDefault(TGeoMatrix::class) {
            if (it == null) {
                TGeoMatrix.serializer().unref(refCache)
            } else {
                error("Unrecognized matrix $it")
            }
        }

        polymorphicDefault(TGeoVolume::class) {
            if (it == null) {
                TGeoVolume.serializer().unref(refCache)
            } else {
                error("Unrecognized volume $it")
            }
        }

        polymorphicDefault(TGeoNode::class) {
            if (it == null) {
                TGeoNode.serializer().unref(refCache)
            } else {
                error("Unrecognized node $it")
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


    fun <T: TObject> decode(sourceDeserializer: KSerializer<T>, source: JsonElement): T {
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

    private fun PolymorphicModuleBuilder<TGeoShape>.shapes() {
        subclass(TGeoBBox.serializer())
        subclass(TGeoCompositeShape.serializer())
        subclass(TGeoXtru.serializer())
        subclass(TGeoTube.serializer())
        subclass(TGeoTubeSeg.serializer())
        subclass(TGeoPcon.serializer())
        subclass(TGeoPgon.serializer())
        subclass(TGeoShapeAssembly.serializer())
    }

    private fun PolymorphicModuleBuilder<TGeoMatrix>.matrices() {
        subclass(TGeoIdentity.serializer())
        subclass(TGeoHMatrix.serializer())
        subclass(TGeoTranslation.serializer())
        subclass(TGeoRotation.serializer())
        subclass(TGeoCombiTrans.serializer())
    }

    private fun PolymorphicModuleBuilder<TGeoBoolNode>.boolNodes() {
        subclass(TGeoIntersection.serializer())
        subclass(TGeoUnion.serializer())
        subclass(TGeoSubtraction.serializer())
    }

    private val serializersModule = SerializersModule {

//        polymorphic(TObject::class) {
//            default { JsonRootSerializer }
//
//            shapes()
//            matrices()
//            boolNodes()
//
//            subclass(TGeoMaterial.serializer())
//            subclass(TGeoMixture.serializer())
//
//            subclass(TGeoMedium.serializer())
//
//            //subclass(TGeoNode.serializer())
//            subclass(TGeoNodeMatrix.serializer())
//            subclass(TGeoVolume.serializer())
//            subclass(TGeoVolumeAssembly.serializer())
//            subclass(TGeoManager.serializer())
//        }

        polymorphic(TGeoShape::class) {
            shapes()
        }

        polymorphic(TGeoMatrix::class) {
            matrices()
        }

        polymorphic(TGeoBoolNode::class) {
            boolNodes()
        }

        polymorphic(TGeoNode::class) {
            subclass(TGeoNodeMatrix.serializer())
            subclass(TGeoNodeOffset.serializer())
        }

        polymorphic(TGeoVolume::class) {
            subclass(TGeoVolume.serializer())
            subclass(TGeoVolumeAssembly.serializer())
        }
    }

    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "_typename"
        serializersModule = this@RootDecoder.serializersModule
    }

}