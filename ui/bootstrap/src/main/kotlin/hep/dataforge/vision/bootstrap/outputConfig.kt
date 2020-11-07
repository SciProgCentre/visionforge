package hep.dataforge.vision.bootstrap

import hep.dataforge.vision.solid.SolidGroup
import hep.dataforge.vision.solid.SolidManager
import hep.dataforge.vision.solid.three.ThreeCanvas
import kotlinx.dom.clear
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
import react.*
import react.dom.button
import react.dom.div
import react.dom.input
import react.dom.label

private fun saveData(event: Event, fileName: String, mimeType: String = "text/plain", dataBuilder: () -> String) {
    event.stopPropagation();
    event.preventDefault();

    val fileSaver = kotlinext.js.require("file-saver")
    val blob = Blob(arrayOf(dataBuilder()), BlobPropertyBag("$mimeType;charset=utf-8"))
    fileSaver.saveAs(blob, fileName)
}

public fun RBuilder.canvasControls(canvas: ThreeCanvas): ReactElement {
    return child(CanvasControls){
        attrs{
            this.canvas = canvas
        }
    }
}

public external interface CanvasControlsProps : RProps {
    public var canvas: ThreeCanvas
}

public val CanvasControls: FunctionalComponent<CanvasControlsProps> = functionalComponent ("CanvasControls") { props ->
    val visionManager = useMemo(
        { props.canvas.context.plugins.fetch(SolidManager).visionManager },
        arrayOf(props.canvas)
    )
    accordion("controls") {
        entry("Settings") {
            div("row") {
                div("col-2") {
                    label("checkbox-inline") {
                        input(type = InputType.checkBox) {
                            attrs {
                                defaultChecked = props.canvas.axes.visible
                                onChangeFunction = {
                                    props.canvas.axes.visible = (it.target as HTMLInputElement).checked
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
                                val json = (props.canvas.content as? SolidGroup)?.let { group ->
                                    visionManager.encodeToString(group)
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
                                        props.canvas.camera.layers.enable(layer)
                                    } else {
                                        props.canvas.camera.layers.disable(layer)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


public fun Element.displayCanvasControls(canvas: ThreeCanvas, block: TagConsumer<HTMLElement>.() -> Unit = {}) {
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
                                    val visionManager = canvas.context.plugins.fetch(SolidManager).visionManager
                                    visionManager.encodeToString(group)
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