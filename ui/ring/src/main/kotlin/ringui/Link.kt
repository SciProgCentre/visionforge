package ringui

import org.w3c.dom.events.MouseEvent
import react.RBuilder
import react.RClass
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

@JsModule("@jetbrains/ring-ui/components/link/link")
internal external object LinkModule {
    @JsName("default")
    val Link: RClass<LinkProps>
}

public fun RBuilder.ringLink(handler: RHandler<LinkProps>) {
    LinkModule.Link {
        handler()
    }
}