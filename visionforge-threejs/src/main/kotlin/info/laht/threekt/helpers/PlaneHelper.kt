@file:JsModule("three")
@file:JsNonModule
package info.laht.threekt.helpers

import info.laht.threekt.math.Color
import info.laht.threekt.math.Plane
import info.laht.threekt.objects.LineSegments

/**
 * Helper object to visualize a [Plane].
 */
external class PlaneHelper(plane : Plane, size : Float, hex : Color): LineSegments{
    var plane: Plane
    var size: Float
}