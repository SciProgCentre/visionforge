@file:JsModule("three")
@file:JsNonModule

package three.extras.curves

import three.extras.core.Curve
import three.math.Vector2

external class SplineCurve(
    points: Array<Vector2> = definedExternally
) : Curve<Vector2> {

    var points: Array<Vector2>

    override fun clone(): SplineCurve
    fun copy(curve: SplineCurve): SplineCurve

}