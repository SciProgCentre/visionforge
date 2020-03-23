package hep.dataforge.vis.editor

import hep.dataforge.js.initState
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.NameToken
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.*

interface MetaViewerProps : RProps {
    var name: NameToken
    var meta: Meta
    var descriptor: NodeDescriptor?
}

class MetaViewerComponent : RComponent<MetaViewerProps, TreeState>() {

    override fun TreeState.init() {
        expanded = false
    }

    override fun RBuilder.render() {
        div("d-inline-block text-truncate") {
            if (props.meta.items.isNotEmpty()) {
                span("objTree-caret") {
                    attrs {
                        classes = if (state.expanded) {
                            setOf("objTree-caret", "objTree-caret-down")
                        } else {
                            setOf("objTree-caret")
                        }
                        onClickFunction = {
                            setState {
                                expanded = !expanded
                            }
                        }
                    }
                }
            }
            label("tree-label") {
                +props.name.toString()
            }
            ul("tree") {
                props.meta.items.forEach { (token, item) ->
                    //val descriptor = props.
                    li {
                        when (item) {
                            is MetaItem.NodeItem -> {
                                child(MetaViewerComponent::class) {
                                    attrs {
                                        name = token
                                        meta = item.node
                                        descriptor = props.descriptor?.nodes?.get(token.body)
                                    }
                                }
                            }
                            is MetaItem.ValueItem -> {
                                div("row") {
                                    div("col") {
                                        label("tree-label") {
                                            +token.toString()
                                        }
                                    }
                                    div("col") {
                                        label {
                                            +item.value.toString()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}