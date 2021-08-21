package ru.mipt.npm.root

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


private typealias  RefCache = MutableList<TObject>

private class RootJsonSerializer<T : TObject>(
    private val refCache: RefCache,
    private val tSerializer: KSerializer<T>
) : KSerializer<T> {

    override val descriptor: SerialDescriptor get() = tSerializer.descriptor

    override fun deserialize(decoder: Decoder): T {
        val input = decoder as JsonDecoder
        val element = input.decodeJsonElement()
        val refId = (element as? JsonObject)?.get("\$ref")?.jsonPrimitive?.int
        return if (refId != null) {
            //Do unref
            val refValue = refCache[refId]
            refValue as T //TODO research means to make it safe
        } else {
            val res = input.json.decodeFromJsonElement(tSerializer, element)
            //val uid = res.fUniqueID
            refCache.add(res)
            res
        }
    }

    override fun serialize(encoder: Encoder, value: T) {
        tSerializer.serialize(encoder, value)
    }

}

private fun <T : TObject> KSerializer<T>.unref(refCache: RefCache): RootJsonSerializer<T> =
    RootJsonSerializer(refCache, this)


private fun PolymorphicModuleBuilder<TGeoShape>.shapes(refCache: RefCache) {
    subclass(TGeoBBox.serializer().unref(refCache))
    subclass(TGeoCompositeShape.serializer().unref(refCache))
    subclass(TGeoXtru.serializer().unref(refCache))
    subclass(TGeoTube.serializer().unref(refCache))
    subclass(TGeoTubeSeg.serializer().unref(refCache))
    subclass(TGeoShapeAssembly.serializer().unref(refCache))
}

private fun PolymorphicModuleBuilder<TGeoMatrix>.matrices(refCache: RefCache) {
    subclass(TGeoIdentity.serializer().unref(refCache))
    subclass(TGeoHMatrix.serializer().unref(refCache))
    subclass(TGeoTranslation.serializer().unref(refCache))
    subclass(TGeoRotation.serializer().unref(refCache))
    subclass(TGeoCombiTrans.serializer().unref(refCache))
}

/**
 * Create an instance of Json with unfolding Root references. This instance could not be reused because of the cache.
 */
internal fun rootJson(): Json {
    val refCache = ArrayList<TObject>(4096)
    return Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "_typename"
        serializersModule = SerializersModule {
            polymorphic(TGeoShape::class) {
                default { TGeoBBox.serializer().unref(refCache) }
                shapes(refCache)
            }

            polymorphic(TGeoMatrix::class) {
                matrices(refCache)
            }

            polymorphic(TObject::class) {
                shapes(refCache)
                matrices(refCache)

                subclass(TGeoMaterial.serializer().unref(refCache))
                subclass(TGeoMixture.serializer().unref(refCache))

                subclass(TGeoMedium.serializer().unref(refCache))

                subclass(TGeoNode.serializer().unref(refCache))
                subclass(TGeoNodeMatrix.serializer().unref(refCache))
                subclass(TGeoVolume.serializer().unref(refCache))
                subclass(TGeoVolumeAssembly.serializer().unref(refCache))
            }
            polymorphic(TGeoNode::class, TGeoNode.serializer().unref(refCache)) {
                subclass(TGeoNodeMatrix.serializer().unref(refCache))
            }
            polymorphic(TGeoVolume::class, TGeoVolume.serializer().unref(refCache)) {
                subclass(TGeoVolumeAssembly.serializer().unref(refCache))
            }
        }
    }
}