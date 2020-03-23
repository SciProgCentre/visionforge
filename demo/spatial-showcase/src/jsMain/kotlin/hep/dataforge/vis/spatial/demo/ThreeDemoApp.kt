package hep.dataforge.vis.spatial.demo

import hep.dataforge.js.Application
import hep.dataforge.js.startApplication
import kotlin.browser.document

private class ThreeDemoApp : Application {

    override fun start(state: Map<String, Any>) {

        val element = document.getElementById("canvas") ?: error("Element with id 'canvas' not found on page")

        ThreeDemoGrid(element).run {
            showcase()
            showcaseCSG()
//            demo("dynamicBox", "Dancing boxes") {
//                val boxes = (-10..10).flatMap { i ->
//                    (-10..10).map { j ->
//                        varBox(10, 10, 0, name = "cell_${i}_${j}") {
//                            x = i * 10
//                            y = j * 10
//                            value = 128
//                            setProperty(EDGES_ENABLED_KEY, false)
//                            setProperty(WIREFRAME_ENABLED_KEY, false)
//                        }
//                    }
//                }
//                GlobalScope.launch {
//                    while (isActive) {
//                        delay(500)
//                        boxes.forEach { box ->
//                            box.value = (box.value + Random.nextInt(-15, 15)).coerceIn(0..255)
//                        }
//                    }
//                }
//            }
        }


    }

}

fun main() {
    startApplication(::ThreeDemoApp)
}