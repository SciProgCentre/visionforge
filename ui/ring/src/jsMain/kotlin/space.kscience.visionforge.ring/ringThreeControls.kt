package space.kscience.visionforge.ring

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import react.FC
import react.Props
import react.RBuilder
import react.dom.attrs
import react.dom.button
import react.fc
import ringui.Island
import ringui.SmartTabs
import ringui.Tab
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.encodeToString
import space.kscience.visionforge.react.flexColumn
import space.kscience.visionforge.react.flexRow
import space.kscience.visionforge.react.propertyEditor
import space.kscience.visionforge.react.visionTree
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import styled.css

internal fun saveData(event: Event, fileName: String, mimeType: String = "text/plain", dataBuilder: () -> String) {
    event.stopPropagation();
    event.preventDefault();

    val fileSaver = kotlinext.js.require<dynamic>("file-saver")
    val blob = Blob(arrayOf(dataBuilder()), BlobPropertyBag("$mimeType;charset=utf-8"))
    fileSaver.saveAs(blob, fileName)
}

internal fun RBuilder.canvasControls(options: Canvas3DOptions, vision: Vision?): Unit {
    child(CanvasControls) {
        attrs {
            this.options = options
            this.vision = vision
        }
    }
}

internal external interface CanvasControlsProps : Props {
    public var options: Canvas3DOptions
    public var vision: Vision?
}

@OptIn(DelicateCoroutinesApi::class)
internal val CanvasControls: FC<CanvasControlsProps> = fc("CanvasControls") { props ->
    flexColumn {
        flexRow {
            css {
                border = Border(1.px, BorderStyle.solid, Color.blue)
                padding = Padding(4.px)
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
            scope = props.vision?.manager?.context ?: GlobalScope,
            properties = props.options.meta,
            descriptor = Canvas3DOptions.descriptor,
            expanded = false
        )

    }
}


public external interface ThreeControlsProps : Props {
    public var canvasOptions: Canvas3DOptions
    public var vision: Vision?
    public var selected: Name?
    public var onSelect: (Name?) -> Unit
    public var additionalTabs: Map<String, RBuilder.() -> Unit>
}

@JsExport
public val ThreeControls: FC<ThreeControlsProps> = fc { props ->
    SmartTabs("Tree") {
        props.vision?.let {
            Tab("Tree") {
                Island("Vision tree") {
                    visionTree(it, props.selected, props.onSelect)
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
): Unit = child(ThreeControls) {
    attrs {
        this.canvasOptions = canvasOptions
        this.vision = vision
        this.selected = selected
        this.onSelect = onSelect
        this.additionalTabs = additionalTabs ?: emptyMap()
    }
}