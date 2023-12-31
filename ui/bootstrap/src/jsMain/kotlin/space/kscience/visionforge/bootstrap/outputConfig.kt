package space.kscience.visionforge.bootstrap

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
import space.kscience.visionforge.Vision
import space.kscience.visionforge.encodeToString
import space.kscience.visionforge.react.flexColumn
import space.kscience.visionforge.react.flexRow
import space.kscience.visionforge.react.propertyEditor
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import styled.css

private fun saveData(event: Event, fileName: String, mimeType: String = "text/plain", dataBuilder: () -> String) {
    event.stopPropagation();
    event.preventDefault();

    val fileSaver = kotlinext.js.require<dynamic>("file-saver")
    val blob = Blob(arrayOf(dataBuilder()), BlobPropertyBag("$mimeType;charset=utf-8"))
    fileSaver.saveAs(blob, fileName)
}

public fun RBuilder.canvasControls(canvasOptions: Canvas3DOptions, vision: Vision?) {
    child(CanvasControls) {
        attrs {
            this.canvasOptions = canvasOptions
            this.vision = vision
        }
    }
}

public external interface CanvasControlsProps : Props {
    public var canvasOptions: Canvas3DOptions
    public var vision: Vision?
}


public val CanvasControls: FC<CanvasControlsProps> = fc("CanvasControls") { props ->
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
        @OptIn(DelicateCoroutinesApi::class)
        propertyEditor(
            scope = props.vision?.manager?.context ?: GlobalScope,
            properties = props.canvasOptions.meta,
            descriptor = Canvas3DOptions.descriptor,
            expanded = false
        )
    }
}