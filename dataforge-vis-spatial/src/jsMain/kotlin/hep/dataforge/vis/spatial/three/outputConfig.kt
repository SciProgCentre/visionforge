package hep.dataforge.vis.spatial.three

import hep.dataforge.vis.js.editor.card
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.dom.append
import kotlinx.html.js.*
import org.w3c.dom.Element
import kotlin.dom.clear

//private fun download(filename: String, text: String) {
//    var element = document.createElement("a");
//    element.setAttribute("href", "data:text/json;charset=utf-8," + encodeURIComponent(text));
//    element.setAttribute("download", filename);
//
//    element.style.display = 'none';
//    document.body.appendChild(element);
//
//    element.click();
//
//    document.body.removeChild(element);
//}

fun Element.threeOutputConfig(canvas: ThreeCanvas) {
    clear()
    append {
        card("Settings"){
            div("row"){
                div("col-1") {
                    label { +"Axes" }
                    input(type = InputType.checkBox).apply {
                        checked = canvas.axes.visible
                        onChangeFunction = {
                            canvas.axes.visible = checked
                        }
                    }
                }
                div("col-1") {
                    button {
                        +"Export"
                        onClickFunction = {

                        }
                    }
                }
            }
        }
        card("Layers"){
            div("row") {
                (0..11).forEach { layer ->
                    div("col-1") {
                        label { +layer.toString() }
                        input(type = InputType.checkBox).apply {
                            if (layer == 0) {
                                checked = true
                            }
                            onChangeFunction = {
                                if (checked) {
                                    canvas.camera.layers.enable(layer)
                                } else {
                                    canvas.camera.layers.disable(layer)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}