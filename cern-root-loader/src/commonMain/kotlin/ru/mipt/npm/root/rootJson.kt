package ru.mipt.npm.root

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*
import kotlin.reflect.KClass


/**
 * Load Json encoded TObject
 */
public fun TObject.Companion.decodeFromJson(serializer: KSerializer<out TObject>, jsonElement: JsonElement): TObject =
    RootDecoder.decode(serializer, jsonElement)

public fun TObject.Companion.decodeFromString(serializer: KSerializer<out TObject>, string: String): TObject {
    val json = RootDecoder.json.parseToJsonElement(string)
    return RootDecoder.decode(serializer, json)
}

private object RootDecoder {

    private class RootUnrefSerializer<T>(
        private val tSerializer: KSerializer<T>,
        private val refCache: MutableList<RefEntry>,// = ArrayList<RefEntry>(4096)
        private val counter: ReferenceCounter
    ) : KSerializer<T> by tSerializer {

        override fun deserialize(decoder: Decoder): T {
            val input = decoder as JsonDecoder
            val element = input.decodeJsonElement()
            val refId = (element as? JsonObject)?.get("\$ref")?.jsonPrimitive?.int
            val ref = if (refId != null) {
                //Do unref
                refCache[refId]
            } else {
                refCache[counter.value].also {
                    counter.increment()
                }
            }
            return ref.value(tSerializer) as T //TODO research means to make it safe
        }
    }

    private fun <T> KSerializer<T>.unref(refCache: MutableList<RefEntry>, counter: ReferenceCounter): KSerializer<T> =
        RootUnrefSerializer(this, refCache, counter)

    @OptIn(ExperimentalSerializationApi::class)
    fun unrefSerializersModule(
        refCache: MutableList<RefEntry>, counter: ReferenceCounter
    ): SerializersModule = SerializersModule {
        val collector = this
        val unrefCollector = object : SerializersModuleCollector {

            override fun <T : Any> contextual(
                kClass: KClass<T>,
                provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>
            ) {
                collector.contextual(kClass) { provider(it).unref(refCache, counter) }
            }

            override fun <Base : Any, Sub : Base> polymorphic(
                baseClass: KClass<Base>,
                actualClass: KClass<Sub>,
                actualSerializer: KSerializer<Sub>
            ) {
                collector.polymorphic(baseClass, actualClass, actualSerializer.unref(refCache, counter))
            }

            override fun <Base : Any> polymorphicDefault(
                baseClass: KClass<Base>,
                defaultSerializerProvider: (className: String?) -> DeserializationStrategy<out Base>?
            ) {
                collector.polymorphicDefault(baseClass) {
                    (defaultSerializerProvider(it) as KSerializer<out Base>).unref(refCache, counter)
                }
            }
        }
        serializersModule.dumpTo(unrefCollector)
    }

    /**
     * Create an instance of Json with unfolding Root references. This instance could not be reused because of the cache.
     */
    private fun unrefJson(refCache: MutableList<RefEntry>, counter: ReferenceCounter): Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "_typename"
        serializersModule = unrefSerializersModule(refCache, counter)
    }


    fun decode(sourceDeserializer: KSerializer<out TObject>, source: JsonElement): TObject {
        val counter = ReferenceCounter()
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
                }
            }
        }
        fillCache(source)

        return unrefJson(refCache, counter).decodeFromJsonElement(sourceDeserializer.unref(refCache, counter), source)
    }

    class ReferenceCounter(var value: Int = 0) {
        fun increment() {
            value += 1
        }

        override fun toString(): String = value.toString()
    }

    class RefEntry(val obj: JsonObject) {

        private var cachedValue: Any? = null

        fun value(serializer: KSerializer<*>): Any {
            if (cachedValue == null) {
                cachedValue = json.decodeFromJsonElement(serializer, obj)
            }
            return cachedValue!!
        }

        override fun toString(): String = obj.toString()
    }

    private fun PolymorphicModuleBuilder<TGeoShape>.shapes() {
        subclass(TGeoBBox.serializer())
        subclass(TGeoCompositeShape.serializer())
        subclass(TGeoXtru.serializer())
        subclass(TGeoTube.serializer())
        subclass(TGeoTubeSeg.serializer())
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
        contextual(TGeoManager::class) { TGeoManager.serializer() }
        contextual(TObjArray::class) { TObjArray.serializer() }
        contextual(TGeoVolumeAssembly::class) { TGeoVolumeAssembly.serializer() }
        contextual(TGeoShapeAssembly::class) { TGeoShapeAssembly.serializer() }
        contextual(TGeoRotation::class) { TGeoRotation.serializer() }
        contextual(TGeoMedium::class) { TGeoMedium.serializer() }

        polymorphic(TGeoShape::class) {
            default { TGeoBBox.serializer() }
            shapes()
        }

        polymorphic(TGeoMatrix::class) {
            matrices()
        }

        polymorphic(TGeoBoolNode::class) {
            boolNodes()
        }

        polymorphic(TObject::class) {
            subclass(TObjArray.serializer())

            shapes()
            matrices()
            boolNodes()

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

    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "_typename"
        serializersModule = this@RootDecoder.serializersModule
    }

}