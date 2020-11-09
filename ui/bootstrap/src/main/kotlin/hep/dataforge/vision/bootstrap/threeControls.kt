package hep.dataforge.vision.bootstrap

import hep.dataforge.names.Name
import hep.dataforge.names.isEmpty
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionGroup
import hep.dataforge.vision.react.objectTree
import hep.dataforge.vision.solid.three.ThreeCanvas
import kotlinx.css.*
import kotlinx.css.properties.border
import react.*
import react.dom.h2
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
    tabPane {
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
                    overflowY = Overflow.auto
                    flex(1.0, 0.0, FlexBasis.auto)
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
                    visionPropertyEditor(
                        selectedObject,
                        default = selectedObject.getAllProperties(),
                        key = selected
                    )
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
    builder: TabBuilder.() -> Unit = {}
): ReactElement = child(ThreeControls) {
    attrs {
        this.canvas = canvas
        this.selected = selected
        this.onSelect = onSelect
    }
    TabBuilder(this).builder()
}