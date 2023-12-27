@file:OptIn(ExperimentalComposeWebApi::class)

package space.kscience.visionforge.gdml.demo

import androidx.compose.runtime.*
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.w3c.files.FileList


//https://codepen.io/zahedkamal87/pen/PobNNwE
@Composable
fun FileDrop(
    title: String = "Drop files or Click here to select files to upload.",
    onFileDrop: (FileList) -> Unit,
) {
    var dragOver by remember { mutableStateOf(false) }

    Div({
        id("dropzone")
        style {
            border(
                width = 0.2.cssRem,
                style = LineStyle.Dashed,
                color = Color("#6583fe")
            )
            padding(2.cssRem)
            borderRadius(0.25.cssRem)
            backgroundColor(Color("#fff"))
            textAlign("center")
            fontSize(1.5.cssRem)
            transitions {
                all {
                    delay(0.25.s)
                    timingFunction(AnimationTimingFunction.EaseInOut)
                    properties("background-color")
                }
            }
            cursor("pointer")
        }
        listOf("drag", "dragstart", "dragend", "dragenter").forEach {
            addEventListener(it) { event ->
                event.preventDefault()
                event.stopPropagation()
            }
        }
        onDragOver { event ->
            event.preventDefault()
            event.stopPropagation()
            dragOver = true
        }
        onDragLeave { event ->
            event.preventDefault()
            event.stopPropagation()
            dragOver = false
        }
        onDrop { event ->
            event.preventDefault()
            event.stopPropagation()
            dragOver = false
            event.dataTransfer?.files?.let {
                onFileDrop(it)
            }
        }
    }) {

        I({ classes("bi", "bi-cloud-upload", "dropzone-icon") })
        Text(title)
        Input(type = InputType.File, attrs = {
            style {
                display(DisplayStyle.None)
            }
            classes("dropzone-input")
            name("files")
        })
    }
}
//
//dropzone.addEventListener("click", function(e) {
//    dropzone_input.click();
//});