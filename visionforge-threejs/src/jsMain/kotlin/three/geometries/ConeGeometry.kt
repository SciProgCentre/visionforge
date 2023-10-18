@file:JsModule("three")
@file:JsNonModule

package three.geometries

import three.core.BufferGeometry


external class ConeGeometry(
    radius: Number = definedExternally,
    height: Number = definedExternally,
    radialSegments: Int = definedExternally,
    heightSegments: Int = definedExternally,
    openEnded: Boolean = definedExternally,
    thetaStart: Boolean = definedExternally,
    thetaLength: Boolean = definedExternally
) : BufferGeometry