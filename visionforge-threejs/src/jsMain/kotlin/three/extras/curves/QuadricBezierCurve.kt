@file:JsModule("three")
@file:JsNonModule

package three.extras.curves

import three.extras.core.Curve
import three.math.Vector2

external class QuadricBezierCurve : Curve<Vector2> {

    constructor(
        v0: Vector2 = definedExternally,
        v1: Vector2 = definedExternally,
        v2: Vector2 = definedExternally
    )

    override fun clone(): QuadricBezierCurve
    fun copy(curve: QuadricBezierCurve3): QuadricBezierCurve

}

