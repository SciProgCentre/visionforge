package space.kscience.visionforge.solid.three

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import space.kscience.visionforge.solid.StlBinarySolid
import space.kscience.visionforge.solid.StlSolid
import space.kscience.visionforge.solid.StlUrlSolid
import three.core.BufferGeometry
import three.external.loaders.STLLoader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

fun ArrayBuffer.toByteArray(): ByteArray = Int8Array(this).unsafeCast<ByteArray>()

public object ThreeStlFactory : ThreeMeshFactory<StlSolid>(StlSolid::class) {

    private val loader = STLLoader().apply {
        requestHeader = listOf(
            "Access-Control-Allow-Origin: *",
        )
    }

    override suspend fun buildGeometry(obj: StlSolid): BufferGeometry = when (obj) {
        is StlBinarySolid -> loader.parse(obj.data)
        is StlUrlSolid -> suspendCoroutine { continuation ->
            loader.load(
                url = obj.url,
                onLoad = {
                    console.info("Loaded STL from ${obj.url}")
                    continuation.resume(it)
                },
                onError = {
                    continuation.resumeWithException(RuntimeException("Failed to load STL object from ${obj.url}"))
                }
            )
        }
    }


}