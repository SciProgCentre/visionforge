@file:JsModule("three")
@file:JsNonModule

package info.laht.threekt.geometries

import info.laht.threekt.core.BufferGeometry

external class PlaneGeometry(

    width: Number,
    height: Number,
    widthSegments: Int = definedExternally,
    heightSegments: Int = definedExternally

) : BufferGeometry