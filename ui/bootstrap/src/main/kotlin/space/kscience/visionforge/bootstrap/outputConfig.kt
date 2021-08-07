package space.kscience.visionforge.bootstrap

import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.padding
import kotlinx.css.properties.border
import kotlinx.css.px
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import react.*
import react.dom.attrs
import react.dom.button
import space.kscience.dataforge.meta.descriptors.defaultNode
import space.kscience.dataforge.meta.withDefault
import space.kscience.visionforge.Vision
import space.kscience.visionforge.react.flexColumn
import space.kscience.visionforge.react.flexRow
import space.kscience.visionforge.react.propertyEditor
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import styled.css

private fun saveData(event: Event, fileName: String, mimeType: String = "text/plain", dataBuilder: () -> String) {
    event.stopPropagation();
    event.preventDefault();

    val fileSaver = kotlinext.js.require("file-saver")
    val blob = Blob(arrayOf(dataBuilder()), BlobPropertyBag("$mimeType;charset=utf-8"))
    fileSaver.saveAs(blob, fileName)
}

public fun RBuilder.canvasControls(canvasOptions: Canvas3DOptions, vision: Vision?): ReactElement {
    return child(CanvasControls) {
        attrs {
            this.canvasOptions = canvasOptions
            this.vision = vision
        }
    }
}

public external interface CanvasControlsProps : RProps {
    public var canvasOptions: Canvas3DOptions
    public var vision: Vision?
}

public val CanvasControls: FunctionComponent<CanvasControlsProps> = functionalComponent("CanvasControls") { props ->
    flexColumn {
        flexRow {
            css {
                border(1.px, BorderStyle.solid, Color.blue)
                padding(4.px)
            }
            props.vision?.manager?.let { manager ->
                button {
                    +"Export"
                    attrs {
                        onClickFunction = {
                            val json = manager.encodeToString(props.vision!!)
                            saveData(it, "object.json", "text/json") {
                                json
                            }
                        }
                    }
                }
            }
        }
        propertyEditor(
            ownProperties = props.canvasOptions,
            allProperties = props.canvasOptions.meta.withDefault(Canvas3DOptions.descriptor.defaultNode),
            descriptor = Canvas3DOptions.descriptor,
            expanded = false
        )
    }
}