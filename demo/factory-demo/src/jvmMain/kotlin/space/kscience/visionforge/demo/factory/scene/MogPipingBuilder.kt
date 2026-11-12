package space.kscience.visionforge.demo.factory.scene

import space.kscience.dataforge.meta.asValue
import space.kscience.dataforge.names.Name
import space.kscience.kmath.geometry.euclidean3d.Float32Vector3D
import space.kscience.visionforge.demo.factory.model.Endpoint
import space.kscience.visionforge.demo.factory.model.StreamSpec
import space.kscience.visionforge.demo.factory.styles.MogStyles
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.useStyle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

public object MogPipingBuilder {

    private fun dist(a: Float32Vector3D, b: Float32Vector3D): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        val dz = a.z - b.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    public fun manhattan(pts: List<Float32Vector3D>): List<Float32Vector3D> {
        if (pts.isEmpty()) return emptyList()
        val out = mutableListOf(pts[0])
        for (i in 1 until pts.size) {
            val q = pts[i]
            val p = out.last()
            var curX = p.x
            var curY = p.y
            var curZ = p.z

            val diffX = abs(q.x - curX) > 1e-6f
            val diffY = abs(q.y - curY) > 1e-6f
            val diffZ = abs(q.z - curZ) > 1e-6f
            val totalDiffs = (if (diffX) 1 else 0) + (if (diffY) 1 else 0) + (if (diffZ) 1 else 0)

            if (diffX && totalDiffs > 1) {
                curX = q.x
                out.add(Float32Vector3D(curX, curY, curZ))
            }
            if (diffY && totalDiffs > 1) {
                curY = q.y
                out.add(Float32Vector3D(curX, curY, curZ))
            }
            if (diffZ && totalDiffs > 1) {
                curZ = q.z
                out.add(Float32Vector3D(curX, curY, curZ))
            }
            out.add(q)
        }

        // Clean close points
        val clean = mutableListOf(out[0])
        for (i in 1 until out.size) {
            val q = out[i]
            if (dist(q, clean.last()) > 1e-3f) {
                clean.add(q)
            }
        }

        if (clean.size <= 2) return clean

        // Merge collinear segments
        val merged = mutableListOf(clean[0])
        for (i in 1 until clean.size - 1) {
            val a = merged.last()
            val b = clean[i]
            val c = clean[i + 1]

            val d1x = b.x - a.x
            val d1y = b.y - a.y
            val d1z = b.z - a.z

            val d2x = c.x - b.x
            val d2y = c.y - b.y
            val d2z = c.z - b.z

            val crossX = d1y * d2z - d1z * d2y
            val crossY = d1z * d2x - d1x * d2z
            val crossZ = d1x * d2y - d1y * d2x
            val crossNorm = sqrt(crossX * crossX + crossY * crossY + crossZ * crossZ)
            val dot = d1x * d2x + d1y * d2y + d1z * d2z

            if (crossNorm < 1e-5f && dot > 0) {
                continue
            }
            merged.add(b)
        }
        merged.add(clean.last())
        return merged
    }

    private fun resolveEndpoint(
        endpoint: Endpoint,
        nozzlesMap: Map<String, CalculatedNozzle>,
    ): Triple<Float32Vector3D, Float32Vector3D?, CalculatedNozzle?> {
        return when (endpoint) {
            is Endpoint.NozzleRef -> {
                val nozzle = nozzlesMap[endpoint.name] ?: error("Nozzle not found: ${endpoint.name}")
                Triple(nozzle.connWorld, nozzle.normalWorld, nozzle)
            }
            is Endpoint.Coordinates -> {
                Triple(Float32Vector3D(endpoint.x.toFloat(), endpoint.y.toFloat(), endpoint.z.toFloat()), null, null)
            }
        }
    }

    public fun SolidGroup.buildPiping(
        streams: List<StreamSpec>,
        nozzlesMap: Map<String, CalculatedNozzle>,
    ) {
        val pipingGroup = solidGroup("Piping") {
            properties.setValue(Name.of("dexpi:class"), "PipingNetworkSystem".asValue())

            for (stream in streams) {
                val streamGroup = solidGroup(stream.id.replace('-', '_')) {
                    properties.setValue(Name.of("dexpi:class"), "PipingNetworkSegment".asValue())
                    properties.setValue(Name.of("dexpi:tagName"), stream.id.asValue())
                    properties.setValue(Name.of("dt:streamId"), stream.id.asValue())
                    properties.setValue(Name.of("dt:nameRu"), stream.name.asValue())
                    properties.setValue(Name.of("dt:medium"), stream.medium.asValue())
                    properties.setValue(Name.of("dt:gostGroup"), stream.gost.asValue())
                    properties.setValue(Name.of("dt:material"), stream.material.asValue())
                    properties.setValue(Name.of("dt:DN_mm"), stream.dn.asValue())
                    properties.setValue(Name.of("dt:flowNominal"), (stream.flow ?: "").asValue())
                    properties.setValue(Name.of("dt:dataStatus"), (stream.status ?: "T").asValue())
                    properties.setValue(Name.of("dt:state"), "run".asValue())
                    stream.boundaryFrom?.let { properties.setValue(Name.of("dt:boundaryFrom"), it.asValue()) }
                    stream.boundaryTo?.let { properties.setValue(Name.of("dt:boundaryTo"), it.asValue()) }

                    useStyle(MogStyles.HIGHLIGHT_STYLE_NAME)
                    useStyle(MogStyles.materialStyleName(stream.material))

                    var kSeg = 0
                    var kElb = 0

                    for ((si, seg) in stream.segments.withIndex()) {
                        val dn = seg.dn ?: stream.dn
                        val r = MogEquipmentBuilder.dnRadius(dn)
                        val ext = MogEquipmentBuilder.extLen(dn).toFloat()

                        val (a, na, ninfoA) = resolveEndpoint(seg.from, nozzlesMap)
                        val (b, nb, ninfoB) = resolveEndpoint(seg.to, nozzlesMap)

                        val a2 = if (na != null) Float32Vector3D(a.x + na.x * ext, a.y + na.y * ext, a.z + na.z * ext) else a
                        val b2 = if (nb != null) Float32Vector3D(b.x + nb.x * ext, b.y + nb.y * ext, b.z + nb.z * ext) else b

                        val pts = mutableListOf<Float32Vector3D>()
                        pts.add(a)
                        pts.add(a2)

                        val route = seg.route
                        if (route?.via != null) {
                            for (v in route.via) {
                                pts.add(Float32Vector3D(v[0].toFloat(), v[1].toFloat(), v[2].toFloat()))
                            }
                        } else if (route?.z != null) {
                            val h = route.z.toFloat()
                            if (route.first == "y") {
                                pts.add(Float32Vector3D(a2.x, a2.y, h))
                                pts.add(Float32Vector3D(a2.x, b2.y, h))
                                pts.add(Float32Vector3D(b2.x, b2.y, h))
                            } else {
                                pts.add(Float32Vector3D(a2.x, a2.y, h))
                                pts.add(Float32Vector3D(b2.x, a2.y, h))
                                pts.add(Float32Vector3D(b2.x, b2.y, h))
                            }
                        }

                        pts.add(b2)
                        pts.add(b)

                        val path = manhattan(pts)

                        for (i in 0 until path.size - 1) {
                            val p = path[i]
                            val q = path[i + 1]

                            val dx = abs(q.x - p.x)
                            val dy = abs(q.y - p.y)
                            val dz = abs(q.z - p.z)

                            val axI = when {
                                dx >= dy && dx >= dz -> 0
                                dy >= dx && dy >= dz -> 1
                                else -> 2
                            }

                            val length = when (axI) {
                                0 -> dx
                                1 -> dy
                                else -> dz
                            }

                            val cx = (p.x + q.x) / 2.0
                            val cy = (p.y + q.y) / 2.0
                            val cz = (p.z + q.z) / 2.0

                            cylinder(r = r, height = length, name = "Seg_${String.format("%02d", kSeg++)}") {
                                x = cx
                                y = cy
                                z = cz
                                when (axI) {
                                    0 -> rotationY = PI / 2
                                    1 -> rotationX = -PI / 2
                                    2 -> {
                                        rotationX = 0
                                        rotationY = 0
                                    }
                                }
                                useStyle(MogStyles.HIGHLIGHT_STYLE_NAME)
                                useStyle(MogStyles.materialStyleName(stream.material))
                            }

                            if (i > 0) {
                                sphere(radius = r, name = "Elbow_${String.format("%02d", kElb++)}") {
                                    x = p.x
                                    y = p.y
                                    z = p.z
                                    useStyle(MogStyles.HIGHLIGHT_STYLE_NAME)
                                    useStyle(MogStyles.materialStyleName(stream.material))
                                }
                            }
                        }

                        // Boundary flanges
                        if (si == 0 && ninfoA == null && stream.boundaryFrom != null && path.size >= 2) {
                            val d0x = path[1].x - path[0].x
                            val d0y = path[1].y - path[0].y
                            val d0z = path[1].z - path[0].z
                            cylinder(r = r * 1.35 + 0.05, height = 0.12, name = "BoundaryFrom") {
                                x = path[0].x
                                y = path[0].y
                                z = path[0].z
                                when {
                                    abs(d0x) >= abs(d0y) && abs(d0x) >= abs(d0z) -> rotationY = PI / 2
                                    abs(d0y) >= abs(d0x) && abs(d0y) >= abs(d0z) -> rotationX = -PI / 2
                                    else -> {
                                        rotationX = 0
                                        rotationY = 0
                                    }
                                }
                                useStyle(MogStyles.HIGHLIGHT_STYLE_NAME)
                                useStyle(MogStyles.materialStyleName("Steel"))
                            }
                        }

                        if (si == stream.segments.size - 1 && ninfoB == null && stream.boundaryTo != null && path.size >= 2) {
                            val lastIdx = path.size - 1
                            val dLastX = path[lastIdx].x - path[lastIdx - 1].x
                            val dLastY = path[lastIdx].y - path[lastIdx - 1].y
                            val dLastZ = path[lastIdx].z - path[lastIdx - 1].z
                            cylinder(r = r * 1.35 + 0.05, height = 0.12, name = "BoundaryTo") {
                                x = path[lastIdx].x
                                y = path[lastIdx].y
                                z = path[lastIdx].z
                                when {
                                    abs(dLastX) >= abs(dLastY) && abs(dLastX) >= abs(dLastZ) -> rotationY = PI / 2
                                    abs(dLastY) >= abs(dLastX) && abs(dLastY) >= abs(dLastZ) -> rotationX = -PI / 2
                                    else -> {
                                        rotationX = 0
                                        rotationY = 0
                                    }
                                }
                                useStyle(MogStyles.HIGHLIGHT_STYLE_NAME)
                                useStyle(MogStyles.materialStyleName("Steel"))
                            }
                        }
                    }
                }
            }
        }
    }
}
