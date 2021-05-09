package ringui

import react.RClass
import ringui.header.HeaderProps

@JsModule("@jetbrains/ring-ui")
public external object RingUI {
    public val Alert: RClass<AlertProps>
    public val Button: RClass<ButtonProps>
    public val Dialog: RClass<DialogProps>
    public val Header: RClass<HeaderProps>
    public val Link: RClass<LinkProps>
    public val Icon: RClass<IconProps>
}
