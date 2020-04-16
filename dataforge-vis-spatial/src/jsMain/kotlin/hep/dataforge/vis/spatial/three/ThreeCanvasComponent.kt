package hep.dataforge.vis.spatial.three

import hep.dataforge.context.Context
import hep.dataforge.names.Name
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.specifications.Canvas
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.findDOMNode
import kotlin.dom.clear

interface ThreeCanvasProps : RProps {
    var context: Context
    var obj: VisualObject3D
    var options: Canvas?
    var selected: Name?
    var clickCallback: (Name?) -> Unit
    var canvasCallback: ((ThreeCanvas?) -> Unit)?
}

interface ThreeCanvasState : RState {
    var element: Element?
//    var canvas: ThreeCanvas?
}

class ThreeCanvasComponent : RComponent<ThreeCanvasProps, ThreeCanvasState>() {

    var canvas: ThreeCanvas? = null

    override fun componentDidMount() {
        val element = state.element as? HTMLElement ?: error("Canvas element not found")
        val three: ThreePlugin = props.context.plugins.load(ThreePlugin)
        canvas = three.output(element, props.options ?: Canvas.empty())
        props.canvasCallback?.invoke(canvas)
        canvas?.render(props.obj)
        canvas?.onClick = props.clickCallback
    }

    override fun componentWillUnmount() {
        state.element?.clear()
        props.canvasCallback?.invoke(null)
    }

    override fun componentDidUpdate(prevProps: ThreeCanvasProps, prevState: ThreeCanvasState, snapshot: Any) {
        if (prevProps.obj != props.obj) {
            componentDidMount()
        }
        if (prevProps.selected != props.selected) {
            canvas?.select(props.selected)
        }
    }

    override fun RBuilder.render() {
        div {
            ref {
                state.element = findDOMNode(it)
            }
        }
    }
}

fun RBuilder.threeCanvas(object3D: VisualObject3D, options: Canvas.() -> Unit = {}) {
    child(ThreeCanvasComponent::class) {
        attrs {
            this.obj = object3D
            this.options = Canvas.invoke(options)
        }
    }
}