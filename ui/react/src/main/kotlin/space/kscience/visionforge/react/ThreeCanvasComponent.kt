package space.kscience.visionforge.react

import kotlinx.css.FlexBasis
import kotlinx.css.flexBasis
import kotlinx.css.flexGrow
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import react.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.fetch
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import space.kscience.visionforge.solid.three.ThreeCanvas
import space.kscience.visionforge.solid.three.ThreePlugin
import styled.css
import styled.styledDiv

public external interface ThreeCanvasProps : RProps {
    public var context: Context
    public var options: Canvas3DOptions?
    public var solid: Solid?
    public var selected: Name?
}

public external interface ThreeCanvasState : RState {
    public var element: Element?
//    var canvas: ThreeCanvas?
}

public val ThreeCanvasComponent: FunctionalComponent<ThreeCanvasProps> = functionalComponent(
    "ThreeCanvasComponent"
) { props ->
    val elementRef = useRef<Element>(null)
    var canvas by useState<ThreeCanvas?>(null)

    val three: ThreePlugin = useMemo({ props.context.fetch(ThreePlugin) }, arrayOf(props.context))

    useEffect(listOf(props.solid, props.options, elementRef)) {
        if (canvas == null) {
            val element = elementRef.current as? HTMLElement ?: error("Canvas element not found")
            canvas = three.getOrCreateCanvas(element, props.options ?: Canvas3DOptions())
        }
    }

    useEffect(listOf(canvas, props.solid)) {
        props.solid?.let { obj ->
            canvas?.render(obj)
        }
    }

    useEffect(listOf(canvas, props.selected)) {
        canvas?.select(props.selected)
    }

    styledDiv {
        css {
            flexGrow = 1.0
            flexBasis = FlexBasis.fill
        }
        ref = elementRef
    }
}