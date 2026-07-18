package space.kscience.visionforge.solid.three.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import app.softwork.bootstrapcompose.Button
import app.softwork.bootstrapcompose.Color
import app.softwork.bootstrapcompose.Column
import app.softwork.bootstrapcompose.Layout.Height
import app.softwork.bootstrapcompose.Layout.Width
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Hr
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.encodeToString
import space.kscience.visionforge.html.*
import space.kscience.visionforge.solid.specifications.Canvas3DOptions

//private val fileSaver = importAsync<dynamic>("file-saver")

@Composable
internal fun CanvasControls(
    vision: Vision?,
    options: Canvas3DOptions,
) {
    val coroutineScope = rememberCoroutineScope()
    Column {
        vision?.let { vision ->
            Button("Export", color = Color.Info, styling = { Layout.width = Width.Full }) {
                val json = vision.encodeToString()

                coroutineScope.launch {
                    FileKit.download(
                        bytes = json.encodeToByteArray(),
                        fileName = options.canvasName + ".json"
                    )
                }
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
