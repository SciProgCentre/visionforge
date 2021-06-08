package ringui.tabs

import react.RBuilder
import react.RClass
import react.RHandler
import react.dom.WithClassName

@JsModule("@jetbrains/ring-ui/components/tabs/tabs")
internal external object TabsModule {
    val Tabs: RClass<TabsProps>
    val Tab: RClass<TabProps>
    val SmartTabs: RClass<SmartTabsProps>
    //val CustomItem: RClass<CustomItemProps>
}

//https://github.com/JetBrains/ring-ui/blob/master/components/tabs/tabs.js
public external interface TabsProps : WithClassName {
    public var theme: String
    public var selected: String
    public var onSelect: (String) -> Unit
    public var href: String
    public var autoCollapse: Boolean
}

public external interface CustomItemProps : WithClassName

public external interface TabProps : WithClassName {
    public var title: dynamic // PropTypes.oneOfType([PropTypes.node, PropTypes.func]).isRequired,
    public var id: String
}

public fun RBuilder.ringTabs(active: String? = null, handler: RHandler<TabsProps>) {
    TabsModule.Tabs {
        active?.let{
            attrs {
                selected = active
            }
        }
        handler()
    }
}

public fun RBuilder.ringTab(title: dynamic, id: String = title.toString(), handler: RHandler<TabProps>) {
    TabsModule.Tab {
        attrs {
            this.title = title
            this.id = id
        }
        handler()
    }
}