package space.kscience.visionforge.solid.three.compose

import androidx.compose.runtime.Composable
import app.softwork.bootstrapcompose.Column
import app.softwork.bootstrapcompose.Layout.Height
import app.softwork.bootstrapcompose.Row
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.compose.*
import space.kscience.visionforge.encodeToString
import space.kscience.visionforge.solid.specifications.Canvas3DOptions

@Composable
internal fun CanvasControls(
    vision: Vision?,
    options: Canvas3DOptions,
) {
    Column {
        Row(attrs = {
            style {
                border {
                    width(1.px)
                    style(LineStyle.Solid)
                    color(Color("blue"))
                }
                padding(4.px)
            }
        }) {
            vision?.let { vision ->
                Button({
                    onClick { event ->
                        val json = vision.encodeToString()
                        event.stopPropagation();
                        event.preventDefault();

                        val fileSaver = kotlinext.js.require<dynamic>("file-saver")
                        val blob = Blob(arrayOf(json), BlobPropertyBag("text/json;charset=utf-8"))
                        fileSaver.saveAs(blob, "object.json") as Unit
                    }
                }) {
                    Text("Export")
                }
            }
        }
        PropertyEditor(
            scope = vision?.manager?.context ?: Global,
            properties = options.meta,
            descriptor = Canvas3DOptions.descriptor,
            expanded = false
        )

    }
}


@Composable
public fun ThreeControls(
    vision: Vision?,
    canvasOptions: Canvas3DOptions,
    selected: Name?,
    onSelect: (Name?) -> Unit,
    tabBuilder: @Composable TabsBuilder.() -> Unit = {},
) {
    Tabs(
        styling = {
            Layout.height = Height.Full
        }
    ) {
        vision?.let { vision ->
            Tab("Tree") {
                CardTitle("Vision tree")
                VisionTree(vision, Name.EMPTY, selected, onSelect)
            }
        }
        Tab("Settings") {
            CardTitle("Canvas configuration")
            CanvasControls(vision, canvasOptions)
        }
        tabBuilder()
    }
}
