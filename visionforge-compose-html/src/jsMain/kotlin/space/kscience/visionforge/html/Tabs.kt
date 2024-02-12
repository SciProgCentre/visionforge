package space.kscience.visionforge.html

import androidx.compose.runtime.*
import app.softwork.bootstrapcompose.Card
import app.softwork.bootstrapcompose.NavbarLink
import app.softwork.bootstrapcompose.Styling
import org.jetbrains.compose.web.css.overflowY
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLDivElement


public class ComposeTab(
    public val key: String,
    public val title: ContentBuilder<HTMLAnchorElement>,
    public val disabled: Boolean,
    public val content: ContentBuilder<HTMLDivElement>,
)

@Composable
public fun Tabs(
    tabs: List<ComposeTab>,
    activeKey: String,
    styling: (Styling.() -> Unit)? = null,
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
) {
    var active by remember(activeKey) { mutableStateOf(activeKey) }

    val activeTab by derivedStateOf { tabs.find { it.key == active } }

    Card(
        styling,
        attrs,
        header = {
            Ul({ classes("nav", "nav-tabs", "card-header-tabs") }) {
                tabs.forEach { tab ->
                    Li({
                        classes("nav-item")
                    }) {
                        NavbarLink(
                            active = active == tab.key,
                            disabled = tab.disabled,
                            attrs = {
                                onClick { event ->
                                    event.preventDefault()
                                    active = tab.key
                                }
                            }
                        ) {
                            tab.title.invoke(this)
                        }
                    }
                }
            }
        },
        bodyAttrs = {
            style {
                overflowY("auto")
            }
        }
    ) {
        activeTab?.content?.invoke(this)
    }
}

public class TabsBuilder {
    internal val tabs: MutableList<ComposeTab> = mutableListOf()

    @Composable
    public fun Tab(
        key: String,
        label: ContentBuilder<HTMLAnchorElement> = { A("#") { Text(key) } },
        disabled: Boolean = false,
        content: ContentBuilder<HTMLDivElement>,
    ) {
        tabs.add(ComposeTab(key, label, disabled, content))
    }

    public fun addTab(tab: ComposeTab) {
        tabs.add(tab)
    }
}

@Composable
public fun Tabs(
    activeKey: String? = null,
    styling: (Styling.() -> Unit)? = null,
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
    builder: @Composable TabsBuilder.() -> Unit,
) {
    val result = TabsBuilder().apply { builder() }

    Tabs(result.tabs, activeKey ?: result.tabs.firstOrNull()?.key ?: "", styling, attrs)
}