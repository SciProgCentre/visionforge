package space.kscience.visionforge.solid.demo

import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import space.kscience.visionforge.Application
import space.kscience.visionforge.solid.x
import space.kscience.visionforge.solid.y
import space.kscience.visionforge.startApplication
import kotlin.random.Random

private class ThreeDemoApp : Application {

    override fun start(state: Map<String, Any>) {

        val element = document.getElementById("demo") ?: error("Element with id 'demo' not found on page")

        ThreeDemoGrid(element).run {
            showcase()
            showcaseCSG()
            demo("dynamicBox", "Dancing boxes") {
                val boxes = (-10..10).flatMap { i ->
                    (-10..10).map { j ->
                        varBox(10, 10, 0, name = "cell_${i}_${j}") {
                            x = i * 10
                            y = j * 10
                            value = 128
                        }
                    }
                }
                GlobalScope.launch {
                    while (isActive) {
                        delay(500)
                        boxes.forEach { box ->
                            box.value = (box.value + Random.nextInt(-15, 15)).coerceIn(1..255)
                        }
                    }
                }
            }
        }
    }
}

fun main() {
    startApplication(::ThreeDemoApp)
}