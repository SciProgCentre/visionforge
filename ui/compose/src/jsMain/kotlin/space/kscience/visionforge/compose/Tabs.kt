package space.kscience.visionforge.compose

import androidx.compose.runtime.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLLIElement


public class ComposeTab(
    public val key: String,
    public val title: String,
    public val content: ContentBuilder<HTMLDivElement>,
    public val disabled: Boolean,
    public val titleExt: ContentBuilder<HTMLLIElement>,
)

@Composable
public fun Tabs(tabs: List<ComposeTab>, activeKey: String) {
    var active by remember(activeKey) { mutableStateOf(activeKey) }

    Div({ classes("card", "text-center") }) {
        Div({ classes("card-header") }) {

            Ul({ classes("nav", "nav-tabs", "card-header-tabs") }) {
                tabs.forEach { tab ->
                    Li({
                        classes("nav-item")
                    }) {
                        A(attrs = {
                            classes("nav-link")
                            if (active == tab.key) {
                                classes("active")
                            }
                            if (tab.disabled) {
                                classes("disabled")
                            }
                            onClick {
                                active = tab.key
                            }
                        }) {
                            Text(tab.title)
                        }
                        tab.titleExt.invoke(this)
                    }
                }
            }
        }
        tabs.find { it.key == active }?.let { tab ->
            Div({ classes("card-body") }) {
                tab.content.invoke(this)
            }
        }
    }


}

public class TabBuilder internal constructor(public val key: String) {
    private var title: String = key
    public var disabled: Boolean = false
    private var content: ContentBuilder<HTMLDivElement> = {}
    private var titleExt: ContentBuilder<HTMLLIElement> = {}

    @Composable
    public fun Content(content: ContentBuilder<HTMLDivElement>) {
        this.content = content
    }

    @Composable
    public fun Title(title: String, titleExt: ContentBuilder<HTMLLIElement> = {}) {
        this.title = title
        this.titleExt = titleExt
    }

    internal fun build(): ComposeTab = ComposeTab(
        key,
        title,
        content,
        disabled,
        titleExt
    )
}

public class TabsBuilder {
    public var active: String = ""
    internal val tabs: MutableList<ComposeTab> = mutableListOf()

    @Composable
    public fun Tab(key: String, builder: @Composable TabBuilder.() -> Unit) {
        tabs.add(TabBuilder(key).apply { builder() }.build())
    }

    public fun addTab(tab: ComposeTab) {
        tabs.add(tab)
    }
}

@Composable
public fun Tabs(builder: @Composable TabsBuilder.() -> Unit) {
    val result = TabsBuilder().apply { builder() }
    Tabs(result.tabs, result.active)
}