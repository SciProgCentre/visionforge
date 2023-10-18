@file:JsModule("three")
@file:JsNonModule

package three.external.loaders

import org.khronos.webgl.ArrayBuffer
import org.w3c.xhr.XMLHttpRequest
import three.animation.AnimationClip
import three.cameras.Camera
import three.loaders.LoadingManager
import three.scenes.Scene

external interface GLTFOnLoadCallback {
    val animations: Array<AnimationClip>
    val scene: Scene
    val scenes: Array<Scene>
    val cameras: Array<Camera>
}

/**
 * A loader for loading glTF 2.0 resource.
 * glTF (GL Transmission Format) is an open format specification for efficient delivery
 * and loading of 3D content. Assets may be provided either in JSON (.gltf) or binary (.glb) format.
 * External files store textures (.jpg, .png, ...) and additional binary data (.bin).
 * A glTF asset may deliver one or more scenes, including meshes, materials, textures, skins,
 * skeletons, morph targets, animations, lights, and/or cameras.
 */
external class GLTFLoader(
    manager: LoadingManager = definedExternally
) {

    /**
     * Begin loading from url and call the callback function with the parsed response content.
     */
    fun load(
        url: String,
        onLoad: (GLTFOnLoadCallback) -> Unit,
        onProgress: (XMLHttpRequest) -> Unit = definedExternally,
        onError: (dynamic) -> Unit = definedExternally
    )

    /**
     * Set the base path for additional resources.
     */
    fun setPath(path: String)

    fun setCrossOrigin(value: String)

    /**
     * Parse a glTF-based ArrayBuffer and fire onLoad callback when complete.
     * The argument to onLoad will be an object that contains loaded parts: .scene, .scenes, .cameras, and .animations.
     */
    fun parse(data: ArrayBuffer, path: String, onLoad: (GLTFOnLoadCallback) -> Unit, onError: (dynamic) -> Unit)

    /**
     * Parse a glTF-based JSON String and fire onLoad callback when complete.
     * The argument to onLoad will be an object that contains loaded parts: .scene, .scenes, .cameras, and .animations.
     */
    fun parse(data: String, path: String, onLoad: (GLTFOnLoadCallback) -> Unit, onError: (dynamic) -> Unit)

}