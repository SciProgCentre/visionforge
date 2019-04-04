@file:JsModule("JSRootGeoBase.js")
@file:JsNonModule

package hep.dataforge.vis.spatial.jsroot

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Object3D

external fun createGeometry(shape: dynamic, limit: Int): BufferGeometry

external fun createCubeBuffer(shape: dynamic, limit: Int): BufferGeometry

external fun createTubeBuffer(shape: dynamic, limit: Int): BufferGeometry

external fun createXtruBuffer(shape: dynamic, limit: Int): BufferGeometry

external fun build(obj: dynamic, opt: dynamic): Object3D