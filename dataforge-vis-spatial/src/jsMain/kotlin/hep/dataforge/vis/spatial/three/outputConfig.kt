package hep.dataforge.vis.spatial.three

import hep.dataforge.js.requireJS
import hep.dataforge.vis.js.editor.accordion
import hep.dataforge.vis.spatial.Visual3D
import hep.dataforge.vis.spatial.VisualGroup3D
import kotlinx.html.InputType
import kotlinx.html.TagConsumer
import kotlinx.html.button
import kotlinx.html.dom.append
import kotlinx.html.js.*
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.dom.clear

private fun saveData(event: Event, fileName: String, mimeType: String = "text/plain", dataBuilder: () -> String) {
    event.stopPropagation();
    event.preventDefault();

    val fileSaver = requireJS("file-saver")
    val blob = Blob(arrayOf(dataBuilder()), BlobPropertyBag("$mimeType;charset=utf-8"))
    fileSaver.saveAs(blob, fileName)
}

fun Element.displayCanvasControls(canvas: ThreeCanvas, block: TagConsumer<HTMLElement>.() -> Unit = {}) {
    clear()
    append {
        accordion("controls") {
            entry("Settings") {
                div("row") {
                    div("col-2") {
                        label("checkbox-inline") {
                            input(type = InputType.checkBox).apply {
                                checked = canvas.axes.visible
                                onChangeFunction = {
                                    canvas.axes.visible = checked
                                }
                            }
                            +"Axes"
                        }
                    }
                    div("col-1") {
                        button {
                            +"Export"
                            onClickFunction = {
                                val json = (canvas.content as? VisualGroup3D)?.let { group ->
                                    Visual3D.json.stringify(
                                        VisualGroup3D.serializer(),
                                        group
                                    )
                                }
                                if (json != null) {
                                    saveData(it, "object.json", "text/json") {
                                        json
                                    }
                                }
                            }
                        }
                    }
                }
            }
            entry("Layers") {
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

/*        card("Settings") {
            div("row") {
                div("col-2") {
                    label("checkbox-inline") {
                        input(type = InputType.checkBox).apply {
                            checked = canvas.axes.visible
                            onChangeFunction = {
                                canvas.axes.visible = checked
                            }
                        }
                        +"Axes"
                    }
                }
                div("col-1") {
                    button {
                        +"Export"
                        onClickFunction = {
                            val json = (canvas.content as? VisualGroup3D)?.let { group ->
                                Visual3D.json.stringify(
                                    VisualGroup3D.serializer(),
                                    group
                                )
                            }
                            if (json != null) {
                                saveData(it, "object.json", "text/json"){
                                    json
                                }
                            }
                        }
                    }
                }
            }
        }
        card("Layers") {
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
        }*/
        block()
    }
}