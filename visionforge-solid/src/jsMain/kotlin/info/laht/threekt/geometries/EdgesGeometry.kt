@file:JsModule("three")
@file:JsNonModule

package info.laht.threekt.geometries

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Geometry

external class EdgesGeometry(geometry: Geometry, thresholdAngle: Int = definedExternally) : BufferGeometry {
    constructor(geometry: BufferGeometry, thresholdAngle: Int = definedExternally)
}