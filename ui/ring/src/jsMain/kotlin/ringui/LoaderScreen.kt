@file:JsModule("@jetbrains/ring-ui/components/loader-screen/loader-screen")
@file:JsNonModule

package ringui

import react.ComponentClass
import react.PropsWithClassName

// https://github.com/JetBrains/ring-ui/blob/master/components/loader-screen/loader-screen.js
public external interface LoaderScreenProps : PropsWithClassName {
    public var containerClassName: String
    public var message: String
}

@JsName("default")
public external val LoaderScreen: ComponentClass<LoaderScreenProps>