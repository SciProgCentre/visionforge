package ru.mipt.npm.root

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement

@Serializable(JsonRootSerializer::class)
public class JsonRootObject(public val element: JsonElement): TObject()

public object JsonRootSerializer: KSerializer<JsonRootObject>{
    private val jsonElementSerializer = JsonElement.serializer()

    override val descriptor: SerialDescriptor
        get() = jsonElementSerializer.descriptor

    override fun deserialize(decoder: Decoder): JsonRootObject {
        return JsonRootObject(decoder.decodeSerializableValue(jsonElementSerializer))
    }

    override fun serialize(encoder: Encoder, value: JsonRootObject) {
        encoder.encodeSerializableValue(jsonElementSerializer, value.element)
    }
}