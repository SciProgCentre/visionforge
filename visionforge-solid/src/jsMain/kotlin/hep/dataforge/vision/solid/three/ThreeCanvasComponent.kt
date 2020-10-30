package hep.dataforge.vision.solid.three

import hep.dataforge.context.Context
import hep.dataforge.names.Name
import hep.dataforge.vision.solid.Solid
import hep.dataforge.vision.solid.specifications.Canvas3DOptions
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import react.*
import react.dom.div

public external interface ThreeCanvasProps : RProps {
    public var context: Context
    public var obj: Solid?
    public var options: Canvas3DOptions?
    public var selected: Name?
    public var clickCallback: (Name?) -> Unit
    public var canvasCallback: ((ThreeCanvas?) -> Unit)?
}

public external interface ThreeCanvasState : RState {
    public var element: Element?
//    var canvas: ThreeCanvas?
}

public val ThreeCanvasComponent: FunctionalComponent<ThreeCanvasProps> = functionalComponent(
    "ThreeCanvasComponent"
) { props ->
    val elementRef = useRef<Element?>(null)
    var canvas by useState<ThreeCanvas?>(null)

    useEffect(listOf(props.context, props.obj, props.options, elementRef)) {
        if (canvas == null) {
            val element = elementRef.current as? HTMLElement ?: error("Canvas element not found")
            val three: ThreePlugin = props.context.plugins.fetch(ThreePlugin)
            val newCanvas = three.output(element, props.options ?: Canvas3DOptions.empty(), props.clickCallback)
            props.canvasCallback?.invoke(newCanvas)
            canvas = newCanvas
        }
    }

    useEffect(listOf(canvas, props.obj)) {
        props.obj?.let { obj ->
            if (canvas?.content != obj) {
                canvas?.render(obj)
            }
        }
    }

    useEffect(listOf(canvas, props.selected)) {
        canvas?.select(props.selected)
    }

    div {
        ref = elementRef
    }
}

//public class ThreeCanvasComponent : RComponent<ThreeCanvasProps, ThreeCanvasState>() {
//
//    private var canvas: ThreeCanvas? = null
//
//    override fun componentDidMount() {
//        props.obj?.let { obj ->
//            if (canvas == null) {
//                val element = state.element as? HTMLElement ?: error("Canvas element not found")
//                val three: ThreePlugin = props.context.plugins.fetch(ThreePlugin)
//                canvas = three.output(element, props.options ?: Canvas3DOptions.empty()).apply {
//                    onClick = props.clickCallback
//                }
//                props.canvasCallback?.invoke(canvas)
//            }
//            canvas?.render(obj)
//        }
//    }
//
//    override fun componentDidUpdate(prevProps: ThreeCanvasProps, prevState: ThreeCanvasState, snapshot: Any) {
//        if (prevProps.obj != props.obj) {
//            componentDidMount()
//        }
//        if (prevProps.selected != props.selected) {
//            canvas?.select(props.selected)
//        }
//    }
//
//    override fun RBuilder.render() {
//        div {
//            ref {
//                state.element = findDOMNode(it)
//            }
//        }
//    }
//}