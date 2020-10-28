package hep.dataforge.vision.solid.three

import hep.dataforge.context.Context
import hep.dataforge.names.Name
import hep.dataforge.vision.solid.Solid
import hep.dataforge.vision.solid.specifications.Canvas3DOptions
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import react.dom.findDOMNode

public external interface ThreeCanvasProps : RProps {
    var context: Context
    var obj: Solid
    var options: Canvas3DOptions?
    var selected: Name?
    var clickCallback: (Name?) -> Unit
    var canvasCallback: ((ThreeCanvas?) -> Unit)?
}

public external interface ThreeCanvasState : RState {
    var element: Element?
//    var canvas: ThreeCanvas?
}

@JsExport
public class ThreeCanvasComponent : RComponent<ThreeCanvasProps, ThreeCanvasState>() {

    private var canvas: ThreeCanvas? = null

    override fun componentDidMount() {
        if(canvas == null) {
            val element = state.element as? HTMLElement ?: error("Canvas element not found")
            val three: ThreePlugin = props.context.plugins.fetch(ThreePlugin)
            canvas = three.output(element, props.options ?: Canvas3DOptions.empty()).apply {
                onClick = props.clickCallback
            }
            props.canvasCallback?.invoke(canvas)
        }
        canvas?.render(props.obj)
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

public fun RBuilder.threeCanvas(object3D: Solid, options: Canvas3DOptions.() -> Unit = {}) {
    child(ThreeCanvasComponent::class) {
        attrs {
            this.obj = object3D
            this.options = Canvas3DOptions.invoke(options)
        }
    }
}