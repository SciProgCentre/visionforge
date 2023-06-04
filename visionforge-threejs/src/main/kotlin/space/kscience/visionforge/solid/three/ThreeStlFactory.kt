package space.kscience.visionforge.solid.three

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import space.kscience.visionforge.solid.StlBinaryVision
import space.kscience.visionforge.solid.StlUrlVision
import space.kscience.visionforge.solid.StlVision
import three.core.BufferGeometry
import three.external.loaders.STLLoader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

fun ArrayBuffer.toByteArray(): ByteArray = Int8Array(this).unsafeCast<ByteArray>()

public object ThreeStlFactory : ThreeMeshFactory<StlVision>(StlVision::class) {

    private val loader = STLLoader().apply {
        requestHeader = listOf("Access-Control-Allow-Origin: *")
    }

    override suspend fun buildGeometry(obj: StlVision): BufferGeometry = when (obj) {
        is StlBinaryVision -> loader.parse(obj.data)
        is StlUrlVision -> suspendCoroutine { continuation ->
            loader.load(
                url = obj.url,
                onLoad = {
                    continuation.resume(it)
                },
                onError = {
                    continuation.resumeWithException(RuntimeException("Failed to load STL object from ${obj.url}"))
                }
            )
        }
    }


}