package ringui.grid

import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

@JsModule("@jetbrains/ring-ui/components/grid/grid")
internal external object GridModule {
    val Grid: RClass<RProps>
    val Row: RClass<RowProps>
    val Col: RClass<dynamic>
}


public fun RBuilder.ringGrid(handler: RHandler<RProps>) {
    GridModule.Grid {
        handler()
    }
}