package ringui

import react.RBuilder
import react.RClass
import react.RHandler
import react.dom.WithClassName

// https://github.com/JetBrains/ring-ui/blob/master/components/alert/alert.js
public external interface AlertProps : WithClassName {
    public var timeout: Number
    public var onCloseRequest: () -> Unit
    public var onClose: () -> Unit
    public var isShaking: Boolean
    public var isClosing: Boolean
    public var inline: Boolean
    public var showWithAnimation: Boolean
    public var closeable: Boolean
    public var type: AlertType
}

public typealias AlertType = String

public object AlertTypes {
    public var ERROR: String = "error"
    public var MESSAGE: String = "message"
    public var SUCCESS: String = "success"
    public var WARNING: String = "warning"
    public var LOADING: String = "loading"
}

@JsModule("@jetbrains/ring-ui/components/alert/alert")
internal external object AlertModule {
    @JsName("default")
    val Alert: RClass<AlertProps>
}

public fun RBuilder.ringAlert(handler: RHandler<AlertProps>) {
    AlertModule.Alert {
        handler()
    }
}