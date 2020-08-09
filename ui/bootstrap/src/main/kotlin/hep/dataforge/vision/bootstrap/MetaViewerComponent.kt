package hep.dataforge.vision.bootstrap

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.NameToken
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.*
import react.dom.*

interface MetaViewerProps : RProps {
    var name: NameToken
    var meta: Meta
    var descriptor: NodeDescriptor?
}


interface TreeState : RState {
    var expanded: Boolean
}

@Deprecated("To be replaced by react functional component")
class MetaViewerComponent : RComponent<MetaViewerProps, TreeState>() {

    override fun TreeState.init() {
        expanded = false
    }

    private val onClick: (Event) -> Unit = {
        setState {
            expanded = !expanded
        }
    }

    override fun RBuilder.render() {
        div("d-inline-block text-truncate") {
            if (props.meta.items.isNotEmpty()) {
                span("tree-caret") {
                    attrs {
                        if (state.expanded) {
                            classes += "tree-caret-down"
                        }
                        onClickFunction = onClick
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