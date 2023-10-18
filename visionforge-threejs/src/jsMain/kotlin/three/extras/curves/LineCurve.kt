@file:JsModule("three")
@file:JsNonModule

package three.extras.curves

import three.extras.core.Curve
import three.math.Vector2

external class LineCurve(
    v1: Vector2 = definedExternally,
    v2: Vector2 = definedExternally
) : Curve<Vector2> {

    override fun clone(): LineCurve
    fun copy(curve: LineCurve): LineCurve

}