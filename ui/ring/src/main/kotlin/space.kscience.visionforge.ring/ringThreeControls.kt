package space.kscience.visionforge.ring

import kotlinx.css.*
import kotlinx.css.properties.border
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import react.*
import react.dom.attrs
import react.dom.button
import ringui.Island
import ringui.SmartTabs
import ringui.Tab
import space.kscience.dataforge.meta.withDefault
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.encodeToString
import space.kscience.visionforge.react.flexColumn
import space.kscience.visionforge.react.flexRow
import space.kscience.visionforge.react.propertyEditor
import space.kscience.visionforge.react.visionTree
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import styled.css
import styled.styledDiv

internal fun saveData(event: Event, fileName: String, mimeType: String = "text/plain", dataBuilder: () -> String) {
    event.stopPropagation();
    event.preventDefault();

    val fileSaver = kotlinext.js.require("file-saver")
    val blob = Blob(arrayOf(dataBuilder()), BlobPropertyBag("$mimeType;charset=utf-8"))
    fileSaver.saveAs(blob, fileName)
}

internal fun RBuilder.canvasControls(options: Canvas3DOptions, vision: Vision?): ReactElement {
    return child(CanvasControls) {
        attrs {
            this.options = options
            this.vision = vision
        }
    }
}

internal external interface CanvasControlsProps : RProps {
    public var options: Canvas3DOptions
    public var vision: Vision?
}

internal val CanvasControls: FunctionalComponent<CanvasControlsProps> = functionalComponent("CanvasControls") { props ->
    flexColumn {
        flexRow {
            css {
                border(1.px, BorderStyle.solid, Color.blue)
                padding(4.px)
            }
            props.vision?.let { vision ->
                button {
                    +"Export"
                    attrs {
                        onClickFunction = {
                            val json = vision.encodeToString()
                            saveData(it, "object.json", "text/json") {
                                json
                            }

                        }
                    }
                }
            }
        }
        propertyEditor(
            ownProperties = props.options,
            allProperties = props.options.withDefault(Canvas3DOptions.descriptor.defaultMeta),
            descriptor = Canvas3DOptions.descriptor,
            expanded = false
        )

    }
}


public external interface ThreeControlsProps : RProps {
    public var canvasOptions: Canvas3DOptions
    public var vision: Vision?
    public var selected: Name?
    public var onSelect: (Name?) -> Unit
    public var additionalTabs: Map<String, RBuilder.() -> Unit>
}

@JsExport
public val ThreeControls: FunctionalComponent<ThreeControlsProps> = functionalComponent { props ->
    SmartTabs("Tree") {
        props.vision?.let {
            Tab("Tree") {
                styledDiv {
                    css {
                        height = 100.pct
                        overflowY = Overflow.auto
                    }
                    Island("Vision tree") {
                        visionTree(it, props.selected, props.onSelect)
                    }
                }
            }
        }
        Tab("Settings") {
            Island("Canvas configuration") {
                canvasControls(props.canvasOptions, props.vision)
            }
        }
        props.additionalTabs.forEach { (name, handler) ->
            Tab(name) {
                handler()
            }
        }
    }
}

public fun RBuilder.ringThreeControls(
    canvasOptions: Canvas3DOptions,
    vision: Vision?,
    selected: Name?,
    onSelect: (Name?) -> Unit = {},
    additionalTabs: Map<String, RBuilder.() -> Unit>? = null
): ReactElement = child(ThreeControls) {
    attrs {
        this.canvasOptions = canvasOptions
        this.vision = vision
        this.selected = selected
        this.onSelect = onSelect
        this.additionalTabs = additionalTabs ?: emptyMap()
    }
}