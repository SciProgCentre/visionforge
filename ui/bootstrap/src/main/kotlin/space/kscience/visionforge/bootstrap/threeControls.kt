package space.kscience.visionforge.bootstrap

import kotlinx.css.*
import kotlinx.css.properties.border
import react.*
import react.dom.h2
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.isEmpty
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionGroup
import space.kscience.visionforge.react.objectTree
import space.kscience.visionforge.solid.three.ThreeCanvas
import styled.css
import styled.styledDiv

public external interface ThreeControlsProps : RProps {
    public var canvas: ThreeCanvas
    public var selected: Name?
    public var onSelect: (Name) -> Unit
}

@JsExport
public val ThreeControls: FunctionalComponent<ThreeControlsProps> = functionalComponent { props ->
    val vision = props.canvas.content
    tabPane(if (props.selected != null) "Properties" else null) {
        tab("Canvas") {
            card("Canvas configuration") {
                canvasControls(props.canvas)
            }
        }
        tab("Tree") {
            css {
                border(1.px, BorderStyle.solid, Color.lightGray)
                padding(10.px)
            }
            h2 { +"Object tree" }
            styledDiv {
                css {
                    flex(1.0, 1.0, FlexBasis.inherit)
                }
                props.canvas.content?.let {
                    objectTree(it, props.selected, props.onSelect)
                }
            }
        }
        tab("Properties") {
            props.selected.let { selected ->
                val selectedObject: Vision? = when {
                    selected == null -> null
                    selected.isEmpty() -> vision
                    else -> (vision as? VisionGroup)?.get(selected)
                }
                if (selectedObject != null) {
                    visionPropertyEditor(selectedObject, key = selected)
                }
            }
        }
        this.parentBuilder.run {
            props.children()
        }
    }
}

public fun RBuilder.threeControls(
    canvas: ThreeCanvas,
    selected: Name?,
    onSelect: (Name) -> Unit = {},
    builder: TabBuilder.() -> Unit = {},
): ReactElement = child(ThreeControls) {
    attrs {
        this.canvas = canvas
        this.selected = selected
        this.onSelect = onSelect
    }
    TabBuilder(this).builder()
}