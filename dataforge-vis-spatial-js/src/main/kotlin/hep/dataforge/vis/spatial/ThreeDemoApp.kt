package hep.dataforge.vis.spatial

import hep.dataforge.context.Global
import hep.dataforge.meta.number
import hep.dataforge.meta.set
import hep.dataforge.vis.ApplicationBase
import hep.dataforge.vis.DisplayGroup
import hep.dataforge.vis.require
import hep.dataforge.vis.spatial.gdml.gdml
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.browser.document
import kotlin.random.Random


class ThreeDemoApp : ApplicationBase() {

    override val stateKeys: List<String> = emptyList()

    override fun start(state: Map<String, Any>) {
        require("JSRootGeoBase.js")

        val renderer = ThreeOutput(Global)
        renderer.start(document.getElementById("canvas")!!)
        println("started")

        lateinit var group: DisplayGroup

        renderer.render {
            group = group {
                box {
                    xSize = 100.0
                    ySize = 100.0
                    zSize = 100.0
                }
                box {
                    x = 110.0
                    xSize = 100.0
                    ySize = 100.0
                    zSize = 100.0
                    properties.style["color"] = 1530
                }
            }
//            convex {
//                point(50, 50, -50)
//                point(50, -50, -50)
//                point(-50, -50, -50)
//                point(-50, 50, -50)
//                point(50, 50, 50)
//                point(50, -50, 50)
//                point(-50, -50, 50)
//                point(-50, 50, 50)
//            }
            gdml {
                y = 110.0
                shape = box(50, 50, 50)
            }
        }

        var color by group.properties.number(1530).int

        GlobalScope.launch {
            val random = Random(111)
            while (isActive) {
                delay(1000)
                color = random.nextInt(0, Int.MAX_VALUE)
            }
        }

//        view.animate()

//        view = WebLinesView(document.getElementById("lines")!!, document.getElementById("addForm")!!)
//        presenter = LinesPresenter(view)
//
//        state["lines"]?.let { linesState ->
//            @Suppress("UNCHECKED_CAST")
//            presenter.restore(linesState as Array<String>)
//        }
    }

    override fun dispose() = emptyMap<String, Any>()//mapOf("lines" to presenter.dispose())
}