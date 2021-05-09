package space.kscience.visionforge.ring

import kotlinx.css.*
import kotlinx.css.properties.border
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import react.*
import react.dom.button
import react.dom.h2
import ringui.island.ringIsland
import ringui.tabs.ringSmartTabs
import ringui.tabs.ringTab
import space.kscience.dataforge.meta.descriptors.defaultMeta
import space.kscience.dataforge.meta.withDefault
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.isEmpty
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionGroup
import space.kscience.visionforge.react.flexColumn
import space.kscience.visionforge.react.flexRow
import space.kscience.visionforge.react.objectTree
import space.kscience.visionforge.react.propertyEditor
import space.kscience.visionforge.solid.SolidGroup
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import space.kscience.visionforge.solid.three.ThreeCanvas
import styled.css
import styled.styledDiv

internal fun saveData(event: Event, fileName: String, mimeType: String = "text/plain", dataBuilder: () -> String) {
    event.stopPropagation();
    event.preventDefault();

    val fileSaver = kotlinext.js.require("file-saver")
    val blob = Blob(arrayOf(dataBuilder()), BlobPropertyBag("$mimeType;charset=utf-8"))
    fileSaver.saveAs(blob, fileName)
}

internal fun RBuilder.canvasControls(canvas: ThreeCanvas): ReactElement {
    return child(CanvasControls) {
        attrs {
            this.canvas = canvas
        }
    }
}

internal external interface CanvasControlsProps : RProps {
    public var canvas: ThreeCanvas
}

internal val CanvasControls: FunctionalComponent<CanvasControlsProps> = functionalComponent("CanvasControls") { props ->
    val visionManager = useMemo(
        { props.canvas.three.solids.visionManager },
        arrayOf(props.canvas)
    )
    flexColumn {
        flexRow {
            css {
                border(1.px, BorderStyle.solid, Color.blue)
                padding(4.px)
            }
            button {
                +"Export"
                attrs {
                    onClickFunction = {
                        val json = (props.canvas.content as? SolidGroup)?.let { group ->
                            visionManager.encodeToString(group)
                        }
                        if (json != null) {
                            saveData(it, "object.json", "text/json") {
                                json
                            }
                        }
                    }
                }
            }
        }
        propertyEditor(
            ownProperties = props.canvas.options,
            allProperties = props.canvas.options.withDefault(Canvas3DOptions.descriptor.defaultMeta()),
            descriptor = Canvas3DOptions.descriptor,
            expanded = false
        )

    }
}


public external interface ThreeControlsProps : RProps {
    public var canvas: ThreeCanvas
    public var selected: Name?
    public var onSelect: (Name) -> Unit
}

@JsExport
public val ThreeControls: FunctionalComponent<ThreeControlsProps> = functionalComponent { props ->
    val vision = props.canvas.content
    ringSmartTabs(if (props.selected != null) "Properties" else null) {
        ringTab("Canvas") {
            ringIsland("Canvas configuration") {
                canvasControls(props.canvas)
            }
        }
        ringTab("Tree") {
            styledDiv {
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
        }
        ringTab("Properties") {
            props.selected.let { selected ->
                val selectedObject: Vision? = when {
                    selected == null -> null
                    selected.isEmpty() -> vision
                    else -> (vision as? VisionGroup)?.get(selected)
                }
                if (selectedObject != null) {
                    ringPropertyEditor(selectedObject, key = selected)
                }
            }
        }
        props.children()
    }
}

public fun RBuilder.ringThreeControls(
    canvas: ThreeCanvas,
    selected: Name?,
    onSelect: (Name) -> Unit = {},
    builder: RBuilder.() -> Unit = {},
): ReactElement = child(ThreeControls) {
    attrs {
        this.canvas = canvas
        this.selected = selected
        this.onSelect = onSelect
    }
    builder()
}