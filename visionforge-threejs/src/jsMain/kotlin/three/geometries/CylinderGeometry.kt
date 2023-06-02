@file:JsModule("three")
@file:JsNonModule

package three.geometries

import three.core.BufferGeometry

external class CylinderGeometry(
    radiusTop: Number,
    radiusBottom: Number,
    height: Number,
    radialSegments: Int = definedExternally,
    heightSegments: Int = definedExternally,
    openEnded: Boolean = definedExternally,
    thetaStart: Number = definedExternally,
    thetaLength: Number = definedExternally
) : BufferGeometry