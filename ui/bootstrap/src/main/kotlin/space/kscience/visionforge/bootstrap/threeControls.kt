package space.kscience.visionforge.bootstrap

import kotlinx.css.*
import kotlinx.css.properties.border
import react.*
import react.dom.h2
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.isEmpty
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionGroup
import space.kscience.visionforge.react.visionTree
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import styled.css
import styled.styledDiv

public external interface ThreeControlsProps : RProps {
    public var canvasOptions: Canvas3DOptions
    public var vision: Vision?
    public var selected: Name?
    public var onSelect: (Name) -> Unit
}

@JsExport
public val ThreeControls: FunctionalComponent<ThreeControlsProps> = functionalComponent { props ->
    tabPane(if (props.selected != null) "Properties" else null) {
        tab("Canvas") {
            card("Canvas configuration") {
                canvasControls(props.canvasOptions, props.vision)
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
                props.vision?.let {
                    visionTree(it, props.selected, props.onSelect)
                }
            }
        }
        tab("Properties") {
            props.selected.let { selected ->
                val selectedObject: Vision? = when {
                    selected == null -> null
                    selected.isEmpty() -> props.vision
                    else -> (props.vision as? VisionGroup)?.get(selected)
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
    canvasOptions: Canvas3DOptions,
    vision: Vision?,
    selected: Name?,
    onSelect: (Name) -> Unit = {},
    builder: TabBuilder.() -> Unit = {},
): ReactElement = child(ThreeControls) {
    attrs {
        this.canvasOptions = canvasOptions
        this.vision = vision
        this.selected = selected
        this.onSelect = onSelect
    }
    TabBuilder(this).builder()
}