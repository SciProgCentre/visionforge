package ringui.tabs

import react.RBuilder
import react.RHandler
import react.dom.WithClassName

public external interface SmartTabsProps : WithClassName {
    public var initSelected: String
}


public fun RBuilder.ringSmartTabs(active: String? = null, handler: RHandler<SmartTabsProps>) {
    TabsModule.SmartTabs {
        active?.let {
            attrs {
                initSelected = active
            }
        }
        handler()
    }
}