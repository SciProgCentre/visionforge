package ru.mipt.npm.muon.monitor

import hep.dataforge.vis.common.removeAll
import hep.dataforge.vis.spatial.*
import ru.mipt.npm.muon.monitor.Monitor.CENTRAL_LAYER_Z
import ru.mipt.npm.muon.monitor.Monitor.LOWER_LAYER_Z
import ru.mipt.npm.muon.monitor.Monitor.UPPER_LAYER_Z
import kotlin.math.PI

class Model {
    private val map = HashMap<String, VisualGroup3D>()
    private val events = HashSet<Event>()

    private fun VisualGroup3D.pixel(pixel: SC1) {
        val group = group(pixel.name) {
            position = Point3D(pixel.center.x, pixel.center.y, pixel.center.z)
            box(pixel.xSize, pixel.ySize, pixel.zSize)
            label(pixel.name) {
                z = - Monitor.PIXEL_Z_SIZE / 2 - 5
                rotationY = PI
            }
        }
        map[pixel.name] = group
    }

    private fun VisualGroup3D.detector(detector: SC16) {
        group(detector.name) {
            detector.pixels.forEach {
                pixel(it)
            }
        }
    }

    var tracks: VisualGroup3D

    val root: VisualGroup3D = VisualGroup3D().apply {
        rotationX = PI / 2
        group("bottom") {
            Monitor.detectors.filter { it.center.z == LOWER_LAYER_Z }.forEach {
                detector(it)
            }
        }

        group("middle") {
            Monitor.detectors.filter { it.center.z == CENTRAL_LAYER_Z }.forEach {
                detector(it)
            }
        }

        group("top") {
            Monitor.detectors.filter { it.center.z == UPPER_LAYER_Z }.forEach {
                detector(it)
            }
        }

        tracks = group("tracks")
    }

    private fun highlight(pixel: String) {
        map[pixel]?.color("blue")
    }

    private fun reset() {
        map.values.forEach {
            it.setProperty(Material3D.MATERIAL_COLOR_KEY, null)
        }
        tracks.removeAll()
    }

    fun showEvent(event: Event) {
        events.add(event)
        event.hits.forEach {
            highlight(it)
        }
        event.track?.let {
            tracks.polyline(*it.toTypedArray())
        }
    }
}