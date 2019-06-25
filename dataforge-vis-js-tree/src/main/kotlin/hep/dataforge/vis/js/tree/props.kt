package hep.dataforge.vis.js.tree

import react.RProps
import react.ReactElement

class ColumnProps(
    var renderCell: (row: Row) -> ReactElement,
    var renderHeaderCell: () -> ReactElement,
    var grow: Number? = null,
    var basis: String? = null // <CSS size> | auto
) : RProps

class TreeTableProps(
    // Model properties
    var value: TreeState,
    var children: Array<ReactElement>,
    var onChange: ((TreeState) -> Unit)? = null,
    // View callbacks
    var onScroll: ((scrollTop: Number) -> Unit)? = null,

    // View properties
    var height: Number? = null, // view height (px)
    var headerHeight: Number? = null, // header height (px)
    var className: String? = null
) : RProps
