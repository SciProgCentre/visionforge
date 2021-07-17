@file:JsModule("three")
@file:JsNonModule

package info.laht.threekt.geometries

import info.laht.threekt.core.BufferGeometry


external class ConeGeometry(
    radius: Number = definedExternally,
    height: Number = definedExternally,
    radialSegments: Int = definedExternally,
    heightSegments: Int = definedExternally,
    openEnded: Boolean = definedExternally,
    thetaStart: Boolean = definedExternally,
    thetaLength: Boolean = definedExternally
) : BufferGeometry