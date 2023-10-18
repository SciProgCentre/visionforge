@file:JsModule("three")
@file:JsNonModule

package three.geometries

import three.core.BufferGeometry

/**
 * This can be used as a helper object to view a Geometry object as a wireframe.
 */
external class WireframeGeometry(geometry: BufferGeometry) : BufferGeometry