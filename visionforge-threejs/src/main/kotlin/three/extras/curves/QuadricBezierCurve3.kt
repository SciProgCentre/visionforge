@file:JsModule("three")
@file:JsNonModule

package three.extras.curves

import three.extras.core.Curve
import three.math.Vector3

external class QuadricBezierCurve3 : Curve<Vector3> {

    constructor(
        v0: Vector3 = definedExternally,
        v1: Vector3 = definedExternally,
        v2: Vector3 = definedExternally
    )

    override fun clone(): QuadricBezierCurve3
    fun copy(curve: QuadricBezierCurve3): QuadricBezierCurve3

}

