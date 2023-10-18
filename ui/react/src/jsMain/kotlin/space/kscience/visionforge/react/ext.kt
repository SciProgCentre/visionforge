package space.kscience.visionforge.react

import react.Props
import react.RBuilder
import react.createElement
import react.dom.client.Root

public fun Root.render(block: RBuilder.() -> Unit) {
    render(createElement<Props>(block))
}