package ringui.header

import react.RElementBuilder
import react.RHandler
import react.dom.WithClassName
import ringui.ButtonProps

// https://github.com/JetBrains/ring-ui/blob/master/components/header/tray.js
public external interface HeaderTrayProps : WithClassName

// https://github.com/JetBrains/ring-ui/blob/master/components/header/tray-icon.js
public external interface HeaderTrayIconProps : ButtonProps {
    public var rotatable: Boolean
}

public fun RElementBuilder<HeaderProps>.ringTray(handler: RHandler<HeaderTrayProps>) {
    HeaderModule.Tray {
        handler()
    }
}

public fun RElementBuilder<HeaderTrayProps>.ringTrayIcon(handler: RHandler<WithClassName>) {
    HeaderModule.TrayIcon {
        handler()
    }
}