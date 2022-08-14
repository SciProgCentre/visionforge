@file:JsModule("three")
@file:JsNonModule

package three.geometries

import three.core.BufferGeometry

external class SphereGeometry(
    radius: Number,
    widthSegments: Int = definedExternally,
    heightSegments: Int = definedExternally,
    phiStart: Number = definedExternally,
    phiLength: Number = definedExternally,
    thetaStart: Number = definedExternally,
    thetaLength: Number = definedExternally
) : BufferGeometry