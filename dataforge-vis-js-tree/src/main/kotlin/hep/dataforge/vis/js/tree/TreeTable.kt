@file:JsModule("cp-react-tree-table")
@file:JsNonModule

package hep.dataforge.vis.js.tree

import react.Component

external interface RowMetadata {
    var depth: Number
    var index: Number
    var height: Number
    var hasChildren: Boolean
}

external interface RowState {
    var isVisible: Boolean
    var top: Number
}

open external class RowModel(data: RowData, metadata: RowMetadata, state: RowState) {
    var data: RowData
    var metadata: RowMetadata
    var `$state`: RowState

    companion object {
        var DEFAULT_HEIGHT: Number
    }
}

external interface RowAPI {
    var toggleChildren: () -> Unit
    var updateData: (newData: RowData) -> Unit
}

external class Row(model: RowModel, api: RowAPI) : RowModel, RowAPI {
    override var toggleChildren: () -> Unit
    override var updateData: (newData: RowData) -> Unit
}

external class Column : Component<ColumnProps, dynamic> {
    override fun render(): dynamic

    companion object {
        var displayName: String
    }
}

external interface TreeNode {
    val data: Any
    val children: Array<TreeNode>?
    val height: Number?
}

external class TreeState(data: Array<RowModel>) {
    var data: Array<RowModel>
    var height: Number
    var hasData: Boolean
    fun findRowModel(node: TreeNode): RowModel?
    fun indexAtYPos(yPos: Number): Number
    fun yPosAtIndex(index: Number): Number

    companion object {
        fun create(data: Array<TreeNode>): TreeState
        fun createEmpty(): TreeState
        fun sliceRows(source: TreeState, from: Number, to: Number): Array<RowModel>
        var _hideRowsInRange: Any
        var _showRowsInRange: Any
        fun expandAll(source: TreeState, depthLimit: Number? = definedExternally /* null */): TreeState
        fun collapseAll(source: TreeState): TreeState
        fun expandAncestors(source: TreeState, model: RowModel): TreeState
        fun toggleChildren(source: TreeState, model: RowModel): TreeState
        fun updateData(source: TreeState, model: RowModel, newData: RowData): TreeState
    }
}

external class TreeTable : Component<TreeTableProps, dynamic> {
    var vListRef: Any
    override fun render(): dynamic
    var handleChange: Any
    fun scrollTo(posY: Number): Unit

    companion object {
        val Column: Column
    }
}