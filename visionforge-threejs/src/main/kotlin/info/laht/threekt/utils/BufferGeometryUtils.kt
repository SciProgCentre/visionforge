@file:JsModule("three/examples/jsm/utils/BufferGeometryUtils")
@file:JsNonModule
package info.laht.threekt.utils

import info.laht.threekt.core.BufferGeometry


public external object BufferGeometryUtils {
    /**
     * Merges a set of geometries into a single instance. All geometries must have compatible attributes. If merge does not succeed, the method returns null.
     * @param geometries -- Array of BufferGeometry instances.
     * @param useGroups -- Whether groups should be generated for the merged geometry or not.
     */
    public fun mergeBufferGeometries(geometries: Array<BufferGeometry>, useGroups: Boolean): BufferGeometry
}