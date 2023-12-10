package space.kscience.visionforge.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.w3c.dom.HTMLDivElement

@Composable
public fun FlexColumn(
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
    content: @Composable ElementScope<HTMLDivElement>.() -> Unit,
): Unit = Div(
    attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
        }
        attrs?.invoke(this)
    },
    content
)

@Composable
public fun FlexRow(
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
    content: @Composable ElementScope<HTMLDivElement>.() -> Unit,
): Unit = Div(
    attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
        }
        attrs?.invoke(this)
    },
    content
)