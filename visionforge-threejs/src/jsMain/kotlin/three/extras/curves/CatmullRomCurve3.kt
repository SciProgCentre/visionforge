@file:JsModule("three")
@file:JsNonModule

package three.extras.curves

import three.extras.core.Curve
import three.math.Vector3


external class CatmullRomCurve3(
    points: Array<Vector3> = definedExternally,
    closed: Boolean = definedExternally,
    curveType: String = definedExternally,
    tension: Double = definedExternally
) : Curve<Vector3> {

    var points: Array<Vector3>
    var closed: Boolean
    var curveType: String
    var tension: Double

    override fun clone(): CatmullRomCurve3
    fun copy(curve: CatmullRomCurve3): CatmullRomCurve3
}