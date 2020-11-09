package hep.dataforge.vision.bootstrap

import hep.dataforge.vision.react.flexColumn
import kotlinx.css.Overflow
import kotlinx.css.flexGrow
import kotlinx.css.overflowY
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.button
import react.dom.li
import react.dom.ul
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv

public external class TabProps : RProps {
    public var id: String
    public var title: String?
}

@JsExport
public val Tab: FunctionalComponent<TabProps> = functionalComponent { props ->
    props.children()
}

public external class TabPaneProps : RProps {
    public var activeTab: String?
}

@JsExport
public val TabPane: FunctionalComponent<TabPaneProps> = functionalComponent("TabPane") { props ->
    var activeTab: String? by useState(props.activeTab)

    val children: Array<out ReactElement?> = Children.map(props.children) {
        it.asElementOrNull()
    } ?: emptyArray()

    val childrenProps = children.mapNotNull {
        it?.props?.unsafeCast<TabProps>()
    }

    flexColumn {
        css {
            flexGrow = 1.0
        }
        ul("nav nav-tabs") {
            childrenProps.forEach { cp ->
                li("nav-item") {
                    button(classes = "nav-link") {
                        +(cp.title ?: cp.id)
                        attrs {
                            if (cp.id == activeTab) {
                                classes += "active"
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
                css {
                    overflowY = Overflow.auto
                }
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
