package space.kscience.visionforge.react

import kotlinx.css.*
import kotlinx.css.properties.TextDecoration
import kotlinx.css.properties.TextDecorationLine
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.*
import react.dom.attrs
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.lastOrNull
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.names.startsWith
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionGroup
import space.kscience.visionforge.asSequence
import space.kscience.visionforge.isEmpty
import styled.css
import styled.styledDiv
import styled.styledSpan

public external interface ObjectTreeProps : Props {
    public var name: Name
    public var selected: Name?
    public var obj: Vision
    public var clickCallback: (Name) -> Unit
}

private val TreeLabel = fc<ObjectTreeProps> { props ->
    val token = useMemo(props.name) { props.name.lastOrNull()?.toString() ?: "World" }
    styledSpan {
        css {
            +TreeStyles.treeLabel
            color = Color("#069")
            cursor = Cursor.pointer
            hover {
                textDecoration = TextDecoration(setOf(TextDecorationLine.underline))
            }
            if (props.name == props.selected) {
                +TreeStyles.treeLabelSelected
            }
        }
        +token
        attrs {
            onClickFunction = { props.clickCallback(props.name) }
        }
    }
}

private fun RBuilder.visionTree(props: ObjectTreeProps): Unit {
    var expanded: Boolean by useState { props.selected?.startsWith(props.name) ?: false }

    val onClick: (Event) -> Unit = {
        expanded = !expanded
    }

    val obj = props.obj

    //display as node if any child is visible
    if (obj is VisionGroup) {
        flexRow {
            if (obj.children.keys.any { !it.body.startsWith("@") }) {
                styledSpan {
                    css {
                        +TreeStyles.treeCaret
                        if (expanded) {
                            +TreeStyles.treeCaredDown
                        }
                    }
                    attrs {
                        onClickFunction = onClick
                    }
                }
            }
            child(TreeLabel, props = props)
        }
        if (expanded) {
            flexColumn {
                css {
                    +TreeStyles.tree
                }
                obj.children.asSequence()
                    .filter { !it.first.toString().startsWith("@") } // ignore statics and other hidden children
                    .sortedBy { (it.second as? VisionGroup)?.children?.isEmpty() ?: true } // ignore empty groups
                    .forEach { (childToken, child) ->
                        styledDiv {
                            css {
                                +TreeStyles.treeItem
                            }
                            child(ObjectTree) {
                                attrs {
                                    this.name = props.name + childToken
                                    this.obj = child
                                    this.selected = props.selected
                                    this.clickCallback = props.clickCallback
                                }
                            }
                        }
                    }
            }
        }
    } else {
        child(TreeLabel, props = props)
    }
}

@JsExport
public val ObjectTree: FC<ObjectTreeProps> = fc("ObjectTree") { props ->
    visionTree(props)
}

public fun RBuilder.visionTree(
    vision: Vision,
    selected: Name? = null,
    clickCallback: (Name) -> Unit = {},
) {
    child(ObjectTree) {
        attrs {
            this.name = Name.EMPTY
            this.obj = vision
            this.selected = selected
            this.clickCallback = clickCallback
        }
    }
}

