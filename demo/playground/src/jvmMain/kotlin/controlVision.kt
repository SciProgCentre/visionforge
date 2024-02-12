package space.kscience.visionforge.examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.html.h2
import kotlinx.html.p
import space.kscience.visionforge.VisionControlEvent
import space.kscience.visionforge.html.*
import space.kscience.visionforge.onSubmit
import kotlin.time.Duration.Companion.seconds


fun main() = serve {

    val events = ArrayDeque<VisionControlEvent>(10)

    val html = VisionOfPlainHtml()

    fun pushEvent(event: VisionControlEvent) {
        events.addFirst(event)
        if (events.size >= 10) {
            events.removeLast()
        }
        html.content {
            events.forEach { event ->
                p {
                    text(event.toString())
                }
            }
        }
    }
    h2 { +"Control elements" }

    vision {
        htmlCheckBox {
            checked = true
            onValueChange(context) {
                pushEvent(this)
            }
        }
    }

    vision {
        htmlRangeField(1, 10) {
            numberValue = 4
            onValueChange(context) {
                pushEvent(this)
            }
            context.launch {
                while (isActive) {
                    delay(1.seconds)
                    numberValue = ((numberValue?.toInt() ?: 0) + 1) % 10
                }
            }
        }
    }


    vision {
        button("Click me") {
            onSubmit(context) {
                pushEvent(this)
            }
        }
    }



    vision(html)
}