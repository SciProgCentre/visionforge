package hep.dataforge.vision.react

import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.html.DIV
import react.RBuilder
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv

inline fun RBuilder.flexColumn(block: StyledDOMBuilder<DIV>.() -> Unit) =
    styledDiv {
        css {
            display = Display.flex
            flexDirection = FlexDirection.column
        }
        block()
    }

inline fun RBuilder.flexRow(block: StyledDOMBuilder<DIV>.() -> Unit) =
    styledDiv {
        css {
            display = Display.flex
            flexDirection = FlexDirection.row
        }
        block()
    }