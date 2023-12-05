@file:JsModule("three")
@file:JsNonModule
package three.helpers

import three.math.Color
import three.math.Plane
import three.objects.LineSegments

/**
 * Helper object to visualize a [Plane].
 */
external class PlaneHelper(plane : Plane, size : Float, hex : Color): LineSegments{
    var plane: Plane
    var size: Float
}