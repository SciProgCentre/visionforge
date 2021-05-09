package ringui.island

import react.RElementBuilder
import react.RHandler
import react.dom.WithClassName

// https://github.com/JetBrains/ring-ui/blob/master/components/island/content.js
public external interface IslandContentProps : WithClassName {
    public var scrollableWrapperClassName: String
    public var fade: Boolean
    public var bottomBorder: Boolean
    public var onScroll: () -> Unit
    public var onScrollToBottom: () -> Unit
}

public fun RElementBuilder<IslandProps>.ringIslandContent(handler: RHandler<IslandContentProps>) {
    IslandModule.Content {
        handler()
    }
}