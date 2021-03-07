package space.kscience.visionforge.react

import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.html.DIV
import react.RBuilder
import react.ReactElement
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv

public inline fun RBuilder.flexColumn(block: StyledDOMBuilder<DIV>.() -> Unit): ReactElement =
    styledDiv {
        css {
            display = Display.flex
            flexDirection = FlexDirection.column
        }
        block()
    }

public inline fun RBuilder.flexRow(block: StyledDOMBuilder<DIV>.() -> Unit): ReactElement =
    styledDiv {
        css {
            display = Display.flex
            flexDirection = FlexDirection.row
        }
        block()
    }