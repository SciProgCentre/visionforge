package hep.dataforge.vision.solid.three

import hep.dataforge.js.requireJS
import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.bootstrap.accordion
import hep.dataforge.vision.bootstrap.entry
import hep.dataforge.vision.solid.SolidGroup
import hep.dataforge.vision.solid.SolidManager
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import react.RBuilder
import react.dom.button
import react.dom.div
import react.dom.input
import react.dom.label
import kotlin.dom.clear

private fun saveData(event: Event, fileName: String, mimeType: String = "text/plain", dataBuilder: () -> String) {
    event.stopPropagation();
    event.preventDefault();

    val fileSaver = requireJS("file-saver")
    val blob = Blob(arrayOf(dataBuilder()), BlobPropertyBag("$mimeType;charset=utf-8"))
    fileSaver.saveAs(blob, fileName)
}

@OptIn(DFExperimental::class)
fun RBuilder.canvasControls(canvas: ThreeCanvas) = accordion("controls") {
    entry("Settings") {
        div("row") {
            div("col-2") {
                label("checkbox-inline") {
                    input(type = InputType.checkBox) {
                        attrs {
                            defaultChecked = canvas.axes.visible
                            onChangeFunction = {
                                canvas.axes.visible = (it.target as HTMLInputElement).checked
                            }
                        }
                    }
                    +"Axes"
                }
            }
            div("col-1") {
                button {
                    +"Export"
                    attrs {
                        onClickFunction = {
                            val json = (canvas.content as? SolidGroup)?.let { group ->
                                SolidManager.jsonForSolids.encodeToString(
                                    SolidGroup.serializer(),
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
    }
    entry("Layers") {
        div("row") {
            (0..11).forEach { layer ->
                div("col-1") {
                    label { +layer.toString() }
                    input(type = InputType.checkBox) {
                        attrs {
                            if (layer == 0) {
                                defaultChecked = true
                            }
                            onChangeFunction = {
                                if ((it.target as HTMLInputElement).checked) {
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


fun Element.displayCanvasControls(canvas: ThreeCanvas, block: TagConsumer<HTMLElement>.() -> Unit = {}) {
    clear()
    append {
        accordion("controls") {
            entry("Settings") {
                div("row") {
                    div("col-2") {
                        label("checkbox-inline") {
                            input(type = InputType.checkBox) {
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
                                val json = (canvas.content as? SolidGroup)?.let { group ->
                                    SolidManager.jsonForSolids.encodeToString(
                                        SolidGroup.serializer(),
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
                            input(type = InputType.checkBox) {
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
        block()
    }
}