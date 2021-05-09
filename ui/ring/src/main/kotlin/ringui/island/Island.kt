package ringui.island

import react.RBuilder
import react.RClass
import react.RHandler
import react.dom.WithClassName

@JsModule("@jetbrains/ring-ui/components/island/island")
internal external object IslandModule {
    val default: RClass<IslandProps>
    val Content: RClass<IslandContentProps>
    val Header: RClass<IslandHeaderProps>
    val AdaptiveIsland: RClass<IslandProps>
}

// https://github.com/JetBrains/ring-ui/blob/master/components/island/island.js
public external interface IslandProps : WithClassName {
    public val narrow: Boolean
    public val withoutPaddings: Boolean
}

public fun RBuilder.ringIsland(handler: RHandler<IslandProps>) {
    IslandModule.default {
        handler()
    }
}

public fun RBuilder.ringIsland(header: String, handler: RHandler<IslandContentProps>) {
    ringIsland {
        ringIslandHeader {
            attrs{
                border = true
            }
            +header
        }
        ringIslandContent(handler)
    }
}


public fun RBuilder.ringAdaptiveIsland(handler: RHandler<IslandProps>) {
    IslandModule.AdaptiveIsland {
        handler()
    }
}