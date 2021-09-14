package space.kscience.visionforge.bootstrap

import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.attrs
import react.dom.button
import react.dom.li
import react.dom.ul
import space.kscience.visionforge.react.flexColumn
import styled.StyledDOMBuilder
import styled.styledDiv

public external interface TabProps : PropsWithChildren {
    public var id: String
    public var title: String?
}

@JsExport
public val Tab: FunctionComponent<TabProps> = functionComponent { props ->
    props.children()
}

public external interface TabPaneProps : PropsWithChildren {
    public var activeTab: String?
}

@JsExport
public val TabPane: FunctionComponent<TabPaneProps> = functionComponent("TabPane") { props ->
    var activeTab: String? by useState(props.activeTab)

    val children: Array<out ReactElement?> = Children.map(props.children) {
        it.asElementOrNull()
    } ?: emptyArray()

    val childrenProps = children.mapNotNull {
        it?.props?.unsafeCast<TabProps>()
    }

    flexColumn {
        ul("nav nav-tabs") {
            childrenProps.forEach { cp ->
                li("nav-item") {
                    button(classes = "nav-link") {
                        +(cp.title ?: cp.id)
                        attrs {
                            if (cp.id == activeTab) {
                                classes = classes + "active"
                            }
                            onClickFunction = {
                                activeTab = cp.id
                            }
                        }
                    }
                }
            }
        }
        children.find { (it?.props?.unsafeCast<TabProps>())?.id == activeTab }?.let {
            child(it)
        }
    }
}

public class TabBuilder(internal val parentBuilder: RBuilder) {
    public fun tab(id: String, title: String? = null, builder: StyledDOMBuilder<DIV>.() -> Unit) {
        parentBuilder.child(Tab) {
            attrs {
                this.id = id
                this.title = title
            }
            styledDiv {
                builder()
            }
        }
    }
}

public inline fun RBuilder.tabPane(activeTab: String? = null, crossinline builder: TabBuilder.() -> Unit) {
    child(TabPane) {
        attrs {
            this.activeTab = activeTab
        }
        TabBuilder(this).builder()
    }
}
