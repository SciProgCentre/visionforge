@file:JsModule("three/examples/jsm/geometries/ConvexGeometry.js")
@file:JsNonModule

package three.external.geometries

import three.core.BufferGeometry
import three.math.Vector3

external class ConvexGeometry(points: Array<Vector3>) : BufferGeometry

external class ConvexBufferGeometry(points: Array<Vector3>) : BufferGeometry