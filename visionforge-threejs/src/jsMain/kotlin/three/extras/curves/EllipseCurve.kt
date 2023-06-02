@file:JsModule("three")
@file:JsNonModule

package three.extras.curves

import three.extras.core.Curve
import three.math.Vector2

open external class EllipseCurve(
    aX: Number = definedExternally,
    aY: Number = definedExternally,
    xRadius: Number = definedExternally,
    yRadius: Number = definedExternally,
    aStartAngle: Number = definedExternally,
    aEndAngle: Number = definedExternally,
    aClockwise: Boolean = definedExternally,
    aRotation: Number = definedExternally

) : Curve<Vector2> {

    var aX: Double
    var aY: Double

    var xRadius: Double
    var yRadius: Double

    var aStartAngle: Double
    var aEndAngle: Double

    var aClockwise: Boolean

    var aRotation: Double

    override fun clone(): EllipseCurve
    fun copy(curve: EllipseCurve): EllipseCurve

}