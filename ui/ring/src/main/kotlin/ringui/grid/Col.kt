package ringui.grid

import react.RBuilder
import react.RHandler
import react.dom.WithClassName

public external interface ColProps : WithClassName {
    public var xs: dynamic // number or boolean
    public var sm: dynamic // number or boolean
    public var md: dynamic // number or boolean
    public var lg: dynamic // number or boolean
    public var xsOffset: Number
    public var smOffset: Number
    public var mdOffset: Number
    public var lgOffset: Number
    public var reverse: Boolean
}

public fun RBuilder.ringCol(handler: RHandler<ColProps>){
    GridModule.Col {
        handler()
    }
}