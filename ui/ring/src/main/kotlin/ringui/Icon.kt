package ringui

import react.RBuilder
import react.RClass
import react.RHandler
import react.dom.WithClassName

// https://github.com/JetBrains/ring-ui/blob/master/components/icon/icon.js
public external interface IconProps : WithClassName {
    public var color: String
    public var glyph: dynamic /* string | func */
    public var height: Number
    public var size: Number
    public var width: Number
    public var loading: Boolean
}

@JsModule("@jetbrains/ring-ui/components/icon/icon")
internal external object IconModule {
    @JsName("default")
    val Icon: RClass<IconProps>
}

public fun RBuilder.ringIcon(handler: RHandler<IconProps>) {
    IconModule.Icon {
        handler()
    }
}