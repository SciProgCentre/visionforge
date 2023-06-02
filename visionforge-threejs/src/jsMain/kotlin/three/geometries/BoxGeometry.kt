@file:JsModule("three")
@file:JsNonModule

package three.geometries

import three.core.BufferGeometry


external class BoxGeometry(
    width: Number,
    height: Number,
    depth: Number,
    widthSegments: Int = definedExternally,
    heightSegments: Int = definedExternally,
    depthSegments: Int = definedExternally
) : BufferGeometry
