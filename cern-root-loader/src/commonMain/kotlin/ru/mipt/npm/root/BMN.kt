package ru.mipt.npm.root

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import space.kscience.visionforge.solid.Float32Vector3D


@Serializable
public data class FairTrackParam(
    val fX: Double,
    val fY: Double,
    val fZ: Double,
    val fTx: Double,
    val fTy: Double,
    val fQp: Double,
)

public fun FairTrackParam.toVector(): Float32Vector3D = Float32Vector3D(fX,fY,fZ)

@Serializable
public data class CbmStsTrack(
    val fParamFirst: FairTrackParam,
    val fParamLast: FairTrackParam,
)

@Serializable
public data class BmnGlobalTrack(
    val fParamFirst: FairTrackParam,
    val fParamLast: FairTrackParam,
)

public class BmnEventContainer(
    public val cbmTracks: List<CbmStsTrack>,
    public val bmnGlobalTracks: List<BmnGlobalTrack>,
)

public object BMN {
    public val json: Json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "_typename"
    }

    public fun readEventJson(string: String): BmnEventContainer {
        val jsonArray = json.parseToJsonElement(string) as JsonArray
        val cbmTracks: List<CbmStsTrack> =
            json.decodeFromJsonElement(ListSerializer(CbmStsTrack.serializer()), jsonArray[0])
        val bmnGlobalTracks: List<BmnGlobalTrack> =
            json.decodeFromJsonElement(ListSerializer(BmnGlobalTrack.serializer()), jsonArray[1])
        return BmnEventContainer(cbmTracks, bmnGlobalTracks)
    }
}