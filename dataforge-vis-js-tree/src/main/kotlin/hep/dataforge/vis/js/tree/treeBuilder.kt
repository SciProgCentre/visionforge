package hep.dataforge.vis.js.tree

import react.RElementBuilder
import react.ReactElement
import react.createElement
import react.dom.span

typealias RowData = Any

class TreeNodeBuilder<D : Any>(override val data: D, override var height: Number? = null) : TreeNode {
    private val _children = ArrayList<TreeNodeBuilder<D>>()
    override val children: Array<TreeNode> get() = _children.toTypedArray()

    fun child(data: D, block: TreeNodeBuilder<D>.() -> Unit = {}) {
        val child = TreeNodeBuilder(data).apply(block)
        _children.add(child)
    }
}


class TreeTableBuilder<D : Any> {
    private val children = ArrayList<TreeNode>()

    fun child(data: D, height: Number? = null, block: TreeNodeBuilder<D>.() -> Unit = {}) {
        this.children.add(TreeNodeBuilder(data, height).apply(block))
    }

    fun build(): TreeState = TreeState.create(children.toTypedArray())
}

fun <D : Any> TreeState.Companion.build(block: TreeTableBuilder<D>.() -> Unit): TreeState {
    return TreeTableBuilder<D>().apply(block).build()
}

fun <D : Any> TreeTableProps.tree(block: TreeTableBuilder<D>.() -> Unit) {
    value = TreeTableBuilder<D>().apply(block).build()
}

fun <D : Any> RElementBuilder<TreeTableProps>.column(
    name: String,
    renderer: (Row) -> ReactElement
): ReactElement {
    val props = ColumnProps(
        renderHeaderCell = { span { +name } },
        renderCell = { row -> renderer.invoke(row) }
    )
    return createElement(TreeTable.Column, props = props)
}

