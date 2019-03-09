@file:JsModule("three")
@file:JsNonModule

package hep.dataforge.vis.spatial.three

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Geometry

external class EdgesGeometry(geometry: Geometry, thresholdAngle: Int = definedExternally) : BufferGeometry {
    constructor(geometry: BufferGeometry, thresholdAngle: Int = definedExternally)
}