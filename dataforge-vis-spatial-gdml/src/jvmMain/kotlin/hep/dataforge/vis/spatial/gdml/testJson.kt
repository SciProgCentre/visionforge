package hep.dataforge.vis.spatial.gdml

import hep.dataforge.names.toName
import hep.dataforge.vis.spatial.*

fun main() {
    val vis = VisualGroup3D().apply {
        val box = Box(100f, 100f, 20f).apply {
            color(0u, 0u, 255u)
        }
        proxy("some.name".toName(), box, "obj")
    }

    val string = Visual3DPlugin.json.stringify(VisualGroup3D.serializer(),vis)
    println(string)
}