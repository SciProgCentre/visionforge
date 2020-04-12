package hep.dataforge.vis.spatial.three

import hep.dataforge.context.Global
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.specifications.Canvas
import kotlinx.html.id
import org.w3c.dom.HTMLElement
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import kotlin.browser.document
import kotlin.dom.clear

interface ThreeCanvasProps : RProps {
    var obj: VisualObject3D
    var canvasId: String
    var options: Canvas
}

class ThreeCanvasComponent : RComponent<ThreeCanvasProps, RState>() {

    private val three: ThreePlugin = Global.plugins.fetch(ThreePlugin)

    override fun componentDidMount() {
        val element = document.getElementById(props.canvasId) as? HTMLElement
            ?: error("Element with id 'canvas' not found on page")
        val output = three.output(element, props.options)
        output.render(props.obj)
    }

    override fun componentWillUnmount() {
        val element = document.getElementById(props.canvasId) as? HTMLElement
            ?: error("Element with id 'canvas' not found on page")
        element.clear()
    }

    override fun RBuilder.render() {
        div {
            attrs {
                id = props.canvasId
            }
        }
    }
}

fun RBuilder.threeCanvas(object3D: VisualObject3D, id: String = "threeCanvas", options: Canvas.() -> Unit = {}) {
    child(ThreeCanvasComponent::class) {
        attrs {
            this.obj = object3D
            this.canvasId = id
            this.options = Canvas.invoke(options)
        }
    }
}