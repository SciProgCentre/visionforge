package ringui

import org.w3c.dom.events.MouseEvent
import react.RBuilder
import react.RHandler
import react.dom.WithClassName

// https://github.com/JetBrains/ring-ui/blob/master/components/link/link.js
public external interface LinkProps : WithClassName {
    public var innerClassName: String
    public var active: Boolean
    var inherit: Boolean
    var pseudo: Boolean
    var hover: Boolean
    var href: String
    var onPlainLeftClick: (MouseEvent) -> Unit
    var onClick: (MouseEvent) -> Unit
}

fun RBuilder.ringLink(handler: RHandler<LinkProps>) {
    RingUI.Link {
        handler()
    }
}