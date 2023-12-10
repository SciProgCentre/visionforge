package space.kscience.visionforge.examples

import kotlinx.html.p
import space.kscience.visionforge.VisionControlEvent
import space.kscience.visionforge.html.*
import space.kscience.visionforge.onClick


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
        }
    }


    vision {
        button("Click me"){
            onClick(context){
                pushEvent(this)
            }
        }
    }



    vision(html)
}