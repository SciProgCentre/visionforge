@file:JsModule("three/examples/jsm/geometries/ConvexGeometry.js")
@file:JsNonModule

package info.laht.threekt.external.geometries

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.math.Vector3

external class ConvexGeometry(points: Array<Vector3>) : BufferGeometry

external class ConvexBufferGeometry(points: Array<Vector3>) : BufferGeometry