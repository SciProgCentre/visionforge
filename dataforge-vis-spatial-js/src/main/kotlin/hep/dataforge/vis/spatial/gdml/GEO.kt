@file:JsModule("JSRootGeoBase.js")
@file:JsNonModule
package hep.dataforge.vis.spatial.gdml

import info.laht.threekt.core.Geometry

external fun createGeometry(shape: dynamic, limit: Int): Geometry
