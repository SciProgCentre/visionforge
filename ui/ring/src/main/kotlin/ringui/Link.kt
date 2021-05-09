package ringui

import org.w3c.dom.events.MouseEvent
import react.RBuilder
import react.RHandler
import react.dom.WithClassName

// https://github.com/JetBrains/ring-ui/blob/master/components/link/link.js
public external interface LinkProps : WithClassName {
    public var innerClassName: String
    public var active: Boolean
    public var inherit: Boolean
    public var pseudo: Boolean
    public var hover: Boolean
    public var href: String
    public var onPlainLeftClick: (MouseEvent) -> Unit
    public var onClick: (MouseEvent) -> Unit
}

public fun RBuilder.ringLink(handler: RHandler<LinkProps>) {
    RingUI.Link {
        handler()
    }
}