import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import space.kscience.dataforge.meta.set
import space.kscience.visionforge.compose.PropertyEditor
import space.kscience.visionforge.solid.specifications.Canvas3DOptions

@Composable
@Preview
fun App(){
    val options = remember {
        Canvas3DOptions{
            meta["custom.field"] = 32
        }
    }
    PropertyEditor(
        properties = options.meta,
        descriptor = Canvas3DOptions.descriptor,
        expanded = true
    )
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        MaterialTheme {
            App()
        }
    }
}
