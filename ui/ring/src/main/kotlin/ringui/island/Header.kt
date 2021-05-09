package ringui.island

import react.RElementBuilder
import react.RHandler
import react.dom.WithClassName

// https://github.com/JetBrains/ring-ui/blob/master/components/island/header.js
public external interface IslandHeaderProps : WithClassName {
    public var border: Boolean
    public var wrapWithTitle: Boolean
    public var phase: Number
}

public fun RElementBuilder<IslandProps>.ringIslandHeader(handler: RHandler<IslandHeaderProps>) {
    IslandModule.Header {
        handler()
    }
}