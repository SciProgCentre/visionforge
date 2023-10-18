package three.geometries

import three.core.BufferGeometry
import three.extras.core.Curve
import three.math.Vector3


/**
 * Creates a tube that extrudes along a 3d curve.
 */
external class TubeGeometry(

    path: Curve<Vector3>,
    tubularSegments: Int = definedExternally,
    radius: Number = definedExternally,
    radiusSegments: Int = definedExternally,
    closed: Boolean = definedExternally

) : BufferGeometry {

    val parameters: dynamic

    val tangents: Array<Vector3>
    val normals: Array<Vector3>
    val binormals: Array<Vector3>

}