@file:JsModule("JSRootGeoBase.js")
@file:JsNonModule

package hep.dataforge.vis.spatial.jsroot

import info.laht.threekt.core.BufferGeometry

external fun createGeometry(shape: dynamic, limit: Int): BufferGeometry
