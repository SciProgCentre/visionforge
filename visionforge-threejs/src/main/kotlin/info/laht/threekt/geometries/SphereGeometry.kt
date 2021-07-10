@file:JsModule("three")
@file:JsNonModule

package info.laht.threekt.geometries

import info.laht.threekt.core.BufferGeometry

external class SphereGeometry(
    radius: Number,
    widthSegments: Int = definedExternally,
    heightSegments: Int = definedExternally,
    phiStart: Number = definedExternally,
    phiLength: Number = definedExternally,
    thetaStart: Number = definedExternally,
    thetaLength: Number = definedExternally
) : BufferGeometry