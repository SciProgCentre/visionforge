@file:JsModule("three")
@file:JsNonModule

package three.geometries

import three.core.BufferGeometry

external class PlaneGeometry(

    width: Number,
    height: Number,
    widthSegments: Int = definedExternally,
    heightSegments: Int = definedExternally

) : BufferGeometry