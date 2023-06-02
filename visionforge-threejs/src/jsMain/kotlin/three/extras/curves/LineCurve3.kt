@file:JsModule("three")
@file:JsNonModule

package three.extras.curves

import three.extras.core.Curve
import three.math.Vector3

external class LineCurve3(
    v1: Vector3 = definedExternally,
    v2: Vector3 = definedExternally
) : Curve<Vector3> {

    override fun clone(): LineCurve3
    fun copy(curve3: LineCurve3): LineCurve3

}

