@file:JsModule("three")
@file:JsNonModule

package info.laht.threekt.geometries

import info.laht.threekt.core.BufferGeometry

/**
 * This can be used as a helper object to view a Geometry object as a wireframe.
 */
external class WireframeGeometry(geometry: BufferGeometry) : BufferGeometry