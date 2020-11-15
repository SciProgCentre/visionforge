package hep.dataforge.vision.react

import hep.dataforge.context.Context
import hep.dataforge.meta.empty
import hep.dataforge.names.Name
import hep.dataforge.vision.solid.Solid
import hep.dataforge.vision.solid.specifications.Canvas3DOptions
import hep.dataforge.vision.solid.three.ThreeCanvas
import hep.dataforge.vision.solid.three.ThreePlugin
import kotlinx.css.*
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import react.*
import styled.css
import styled.styledDiv

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
            val newCanvas: ThreeCanvas =
                three.attachRenderer(element, props.options ?: Canvas3DOptions.empty(), props.clickCallback)
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

    styledDiv {
        css {
            minWidth = 500.px
            minHeight = 500.px
            display = Display.inherit
            flex(1.0, 1.0, FlexBasis.auto)
        }
        ref = elementRef
    }
}