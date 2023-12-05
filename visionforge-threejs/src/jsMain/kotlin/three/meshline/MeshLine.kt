@file:JsModule("three.meshline")
@file:JsNonModule

package three.meshline

import three.core.BufferGeometry
import three.materials.ShaderMaterial
import three.math.Color
import three.math.Vector3
import three.textures.Texture

/*
 * https://github.com/spite/THREE.MeshLine
 */

public external class MeshLine : BufferGeometry {
    public fun setGeometry(geometry: BufferGeometry)
    public fun setPoints(points: Array<Vector3>)
}

public external class MeshLineMaterial : ShaderMaterial {
    @JsName("lineWidth")
    public var thickness: Float
    public var color: Color

    public var map: Texture?
    public var useMap: Boolean
    public var alphaMap: Texture?
    public var useAlphaMap: Boolean

    public var repeat: dynamic // - THREE.Vector2 to define the texture tiling (applies to map and alphaMap - MIGHT CHANGE IN THE FUTURE)
    public var dashArray: dynamic //- the length and space between dashes. (0 - no dash)
    public var dashOffset: dynamic // - defines the location where the dash will begin. Ideal to animate the line.
    public var dashRatio: dynamic // - defines the ratio between that is visible or not (0 - more visible, 1 - more invisible).
    public var resolution: dynamic // - THREE.Vector2 specifying the canvas size (REQUIRED)
    public var sizeAttenuation: Int // - makes the line width constant regardless distance (1 unit is 1px on screen) (0 - attenuate, 1 - don't attenuate)
}