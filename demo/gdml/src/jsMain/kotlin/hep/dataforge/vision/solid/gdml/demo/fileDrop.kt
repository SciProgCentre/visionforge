package hep.dataforge.vision.solid.gdml.demo

import drop.FileDrop
import kotlinx.css.*
import kotlinx.css.properties.border
import org.w3c.files.FileList
import react.RBuilder
import styled.css
import styled.styledDiv

//TODO move styles to inline

fun RBuilder.fileDrop(title: String, action: (files: FileList?) -> Unit) {
    styledDiv {
        css {
            border(style = BorderStyle.dashed, width = 1.px, color = Color.orange)
            alignContent = Align.center
        }

        child(FileDrop::class) {
            attrs {
                onDrop = { files, _ ->
                    console.info("loaded $files")
                    action(files)
                }
            }
            +title
        }
    }
}