package ringui.grid

import react.RBuilder
import react.RHandler
import react.dom.WithClassName

public enum class RowPosition {
    xs,
    sm,
    md,
    lg
}

public external interface RowProps : WithClassName {
    public var reverse: Boolean
    public var start: RowPosition
    public var center: RowPosition
    public var end: RowPosition
    public var top: RowPosition
    public var middle: RowPosition
    public var baseline: RowPosition
    public var bottom: RowPosition
    public var around: RowPosition
    public var between: RowPosition
    public var first: RowPosition
    public var last: RowPosition
}

public fun RBuilder.ringRow(handler: RHandler<RowProps>){
    GridModule.Row {
        handler()
    }
}