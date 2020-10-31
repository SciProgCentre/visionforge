package ringui.header

import kotlinx.html.A
import react.RElementBuilder
import react.RHandler
import ringui.IconProps
import styled.StyledDOMBuilder

external interface HeaderLogoProps : IconProps

fun StyledDOMBuilder<A>.ringLogo(handler: RHandler<HeaderLogoProps>) {
    HeaderModule.Logo {
        handler()
    }
}

fun RElementBuilder<HeaderProps>.ringLogo(handler: RHandler<HeaderLogoProps>) {
    HeaderModule.Logo {
        handler()
    }
}
