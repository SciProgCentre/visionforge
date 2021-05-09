package ringui.header

import kotlinx.html.A
import react.RElementBuilder
import react.RHandler
import ringui.IconProps
import styled.StyledDOMBuilder

public external interface HeaderLogoProps : IconProps

public fun StyledDOMBuilder<A>.ringLogo(handler: RHandler<HeaderLogoProps>) {
    HeaderModule.Logo {
        handler()
    }
}

public fun RElementBuilder<HeaderProps>.ringLogo(handler: RHandler<HeaderLogoProps>) {
    HeaderModule.Logo {
        handler()
    }
}
