package hep.dataforge.vis.js.tree.demo

import hep.dataforge.vis.js.tree.ApplicationBase
import hep.dataforge.vis.js.tree.TreeTable
import hep.dataforge.vis.js.tree.column
import hep.dataforge.vis.js.tree.tree
import react.dom.render
import react.dom.span
import kotlin.browser.document


data class TableData(val name: String, val col1: String, val col2: Int)

class TreeDemoApp : ApplicationBase() {

    override val stateKeys: List<String> = emptyList()

    override fun start(state: Map<String, Any>) {

        println("Starting application")
        val element = document.getElementById("demo")!!

        println("Started application")

        println("${TreeTable::class} is loaded")



        render(element) {
            child(TreeTable::class) {
                attrs {
                    tree<TableData> {
                        child(TableData("aaa", "bbb", 2))
                        child(TableData("ccc", "ddd", 66)) {
                            child(TableData("ddd", "ggg", 22))
                        }
                    }
                    onScroll = {}
                }
                column<TableData>("title"){
                    span { +(it.data as TableData).name }
                }
                column<TableData>("col1"){
                    span { +(it.data as TableData).col1 }
                }
                column<TableData>("col2"){
                    span { +(it.data as TableData).col2 }
                }
            }
        }
    }

    override fun dispose() = emptyMap<String, Any>()//mapOf("lines" to presenter.dispose())
}