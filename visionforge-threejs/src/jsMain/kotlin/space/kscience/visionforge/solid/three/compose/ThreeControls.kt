package space.kscience.visionforge.solid.three.compose

import androidx.compose.runtime.Composable
import app.softwork.bootstrapcompose.Button
import app.softwork.bootstrapcompose.Color.Info
import app.softwork.bootstrapcompose.Column
import app.softwork.bootstrapcompose.Layout.Height
import app.softwork.bootstrapcompose.Layout.Width
import org.jetbrains.compose.web.dom.Hr
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.encodeToString
import space.kscience.visionforge.html.*
import space.kscience.visionforge.solid.specifications.Canvas3DOptions

@Composable
internal fun CanvasControls(
    vision: Vision?,
    options: Canvas3DOptions,
) {
    Column {
        vision?.let { vision ->
            Button("Export", color = Info, styling = { Layout.width = Width.Full }) {
                val json = vision.encodeToString()

                val fileSaver = kotlinext.js.require<dynamic>("file-saver")
                val blob = Blob(arrayOf(json), BlobPropertyBag("text/json;charset=utf-8"))
                fileSaver.saveAs(blob, "${options.canvasName}.json") as Unit
            }
        }
        Hr()
        PropertyEditor(
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
