package hep.dataforge.vision.bootstrap

import hep.dataforge.vision.react.flexColumn
import hep.dataforge.vision.react.flexRow
import hep.dataforge.vision.solid.SolidGroup
import hep.dataforge.vision.solid.SolidManager
import hep.dataforge.vision.solid.three.ThreeCanvas
import kotlinx.css.*
import kotlinx.css.properties.border
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import react.*
import react.dom.button
import react.dom.h3
import react.dom.input
import react.dom.label
import styled.css
import styled.styledDiv

private fun saveData(event: Event, fileName: String, mimeType: String = "text/plain", dataBuilder: () -> String) {
    event.stopPropagation();
    event.preventDefault();

    val fileSaver = kotlinext.js.require("file-saver")
    val blob = Blob(arrayOf(dataBuilder()), BlobPropertyBag("$mimeType;charset=utf-8"))
    fileSaver.saveAs(blob, fileName)
}

public fun RBuilder.canvasControls(canvas: ThreeCanvas): ReactElement {
    return child(CanvasControls) {
        attrs {
            this.canvas = canvas
        }
    }
}

public external interface CanvasControlsProps : RProps {
    public var canvas: ThreeCanvas
}

public val CanvasControls: FunctionalComponent<CanvasControlsProps> = functionalComponent("CanvasControls") { props ->
    val visionManager = useMemo(
        { props.canvas.context.plugins.fetch(SolidManager).visionManager },
        arrayOf(props.canvas)
    )
    flexColumn {
        h3 { +"Axes" }
        flexRow {
            css{
                border(1.px,BorderStyle.solid, Color.blue)
                padding(4.px)
            }
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
        h3 { +"Export" }
        flexRow {
            css{
                border(1.px,BorderStyle.solid, Color.blue)
                padding(4.px)
            }
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
        h3 { +"Layers" }
        flexRow {
            css {
                flexWrap = FlexWrap.wrap
                border(1.px,BorderStyle.solid, Color.blue)
                padding(4.px)
            }
            (0..31).forEach { layer ->
                styledDiv {
                    css{
                        padding(4.px)
                    }
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