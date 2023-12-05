@file:JsModule("three")
@file:JsNonModule

package three.geometries

import three.core.BufferGeometry


external class TorusGeometry(
    radius: Number = definedExternally,
    tube: Number = definedExternally,
    radialSegments: Int = definedExternally,
    tubularSegments: Int = definedExternally,
    arc: Number = definedExternally
) : BufferGeometry