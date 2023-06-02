@file:JsModule("three")
@file:JsNonModule

package three.extras.core

import three.math.Vector2

open external class Path : CurvePath<Vector2> {

    var currentPoint: Vector2

}