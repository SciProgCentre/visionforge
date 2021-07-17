@file:JsModule("three")
@file:JsNonModule

package info.laht.threekt.geometries

import info.laht.threekt.core.BufferGeometry


external class BoxGeometry(
    width: Number,
    height: Number,
    depth: Number,
    widthSegments: Int = definedExternally,
    heightSegments: Int = definedExternally,
    depthSegments: Int = definedExternally
) : BufferGeometry
