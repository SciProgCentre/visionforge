@file:UseSerializers(Point3DSerializer::class)
package ru.mipt.npm.muon.monitor

import hep.dataforge.vis.spatial.Point3D
import hep.dataforge.vis.spatial.Point3DSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class Line(
    val start: Point3D,
    val direction: Point3D
)

data class Plane(
    val normal: Point3D,
    val offset: Number
)
