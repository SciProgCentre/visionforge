@file:JsModule("three-full")
@file:JsNonModule
package hep.dataforge.vis.spatial.three

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Geometry
import info.laht.threekt.math.Vector3

external class ConvexGeometry(points: Array<Vector3>) : Geometry

external class ConvexBufferGeometry(points: Array<Vector3>) : BufferGeometry