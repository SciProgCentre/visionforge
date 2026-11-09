package space.kscience.visionforge.demo.factory.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
public data class FactorySceneConfig(
    val schema: String,
    val meta: JsonObject = JsonObject(emptyMap()),
    val layout: FactoryLayout,
    val materials: Map<String, MaterialSpec> = emptyMap(),
    val gost14202: Map<String, GostSpec> = emptyMap(),
    val states: Map<String, StateSpec> = emptyMap(),
    val equipment: List<EquipmentSpec> = emptyList(),
    val streams: List<StreamSpec> = emptyList(),
) {
    public companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            allowSpecialFloatingPointValues = true
        }

        public fun parse(jsonString: String): FactorySceneConfig =
            json.decodeFromString(serializer(), jsonString)

        public fun loadFromResources(resourcePath: String = "mog_scene_params.json"): FactorySceneConfig {
            val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)
                ?: FactorySceneConfig::class.java.classLoader.getResourceAsStream(resourcePath)
                ?: error("Resource not found: $resourcePath")
            val content = stream.bufferedReader().use { it.readText() }
            return parse(content)
        }
    }
}

@Serializable
public data class FactoryLayout(
    val lineSpacing_m: Double = 42.0,
    val lines: List<Int> = listOf(1, 2, 3),
    val slab: SlabSpec,
)

@Serializable
public data class SlabSpec(
    val x: List<Double>,
    val y: List<Double>,
    val thickness: Double = 0.3,
)

@Serializable
public data class MaterialSpec(
    val ru: String,
    val rgb: List<Double>,
    val metallic: Double = 0.0,
    val roughness: Double = 0.5,
)

@Serializable
public data class GostSpec(
    val ru: String,
    val rgb: List<Double>,
)

@Serializable
public data class StateSpec(
    val ru: String,
    val rgb: List<Double>,
)

@Serializable
public data class NozzleSpec(
    val id: String,
    val port: String,
    val dn: Int,
    val face: String,
    val z: Double? = null,
    val t: Double? = null,
    val off: List<Double>? = null,
)

@Serializable
public data class EquipmentSpec(
    val id: String,
    val name: String,
    val dexpi: String,
    val nodeType: String,
    val template: String,
    val pos: List<Double>,
    val rotZ: Double? = null,
    val d: Double? = null,
    val shellH: Double? = null,
    val skirtTop: Double? = null,
    val headH: Double? = null,
    val h: Double? = null,
    val roofH: Double? = null,
    val base: Double? = null,
    val legsH: Double? = null,
    val hopperH: Double? = null,
    val inletH: Double? = null,
    val tubeH: Double? = null,
    val hoodH: Double? = null,
    val wallH: Double? = null,
    val coneH: Double? = null,
    val platformZ: Double? = null,
    @SerialName("L") val length: Double? = null,
    @SerialName("W") val width: Double? = null,
    @SerialName("H") val height: Double? = null,
    val small: Boolean? = null,
    val material: String,
    val frameMaterial: String? = null,
    val status: String? = null,
    val state: String = "run",
    val nozzles: List<NozzleSpec> = emptyList(),
    val attrs: JsonObject = JsonObject(emptyMap()),
    val signals: List<String> = emptyList(),
)

@Serializable
public data class RouteSpec(
    val via: List<List<Double>>? = null,
    val z: Double? = null,
    val first: String? = null,
)

@Serializable
public sealed interface Endpoint {
    public data class NozzleRef(val name: String) : Endpoint
    public data class Coordinates(val x: Double, val y: Double, val z: Double) : Endpoint {
        public fun toList(): List<Double> = listOf(x, y, z)
    }
}

public object EndpointSerializer : KSerializer<Endpoint> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Endpoint", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Endpoint {
        val jsonInput = decoder as? JsonDecoder ?: error("Can only deserialize with JsonDecoder")
        return when (val element = jsonInput.decodeJsonElement()) {
            is JsonPrimitive -> {
                Endpoint.NozzleRef(element.content)
            }
            is JsonArray -> {
                val coords = element.map { (it as JsonPrimitive).double }
                Endpoint.Coordinates(coords[0], coords[1], coords[2])
            }
            else -> error("Unexpected JSON element for Endpoint: $element")
        }
    }

    override fun serialize(encoder: Encoder, value: Endpoint) {
        val jsonOutput = encoder as? JsonEncoder ?: error("Can only serialize with JsonEncoder")
        when (value) {
            is Endpoint.NozzleRef -> jsonOutput.encodeJsonElement(JsonPrimitive(value.name))
            is Endpoint.Coordinates -> jsonOutput.encodeJsonElement(
                JsonArray(listOf(JsonPrimitive(value.x), JsonPrimitive(value.y), JsonPrimitive(value.z)))
            )
        }
    }
}

@Serializable
public data class SegmentSpec(
    @Serializable(with = EndpointSerializer::class)
    val from: Endpoint,
    @Serializable(with = EndpointSerializer::class)
    val to: Endpoint,
    val dn: Int? = null,
    val route: RouteSpec? = null,
)

@Serializable
public data class StreamSpec(
    val id: String,
    val name: String,
    val gost: String,
    val medium: String,
    val material: String,
    val dn: Int,
    val status: String? = null,
    val flow: String? = null,
    val boundaryFrom: String? = null,
    val boundaryTo: String? = null,
    val segments: List<SegmentSpec> = emptyList(),
)
