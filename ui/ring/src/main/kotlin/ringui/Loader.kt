@file:JsModule("@jetbrains/ring-ui/components/loader/loader")
@file:JsNonModule

package ringui

import react.ComponentClass
import react.dom.WithClassName

// https://github.com/JetBrains/ring-ui/blob/master/components/loader/loader.js
public external interface LoaderProps : WithClassName {
    public var size: Number
    public var colors: Array<String>
    public var message: String
    public var stop: Boolean
    public var deterministic: Boolean
}

@JsName("default")
public external val Loader: ComponentClass<LoaderProps>