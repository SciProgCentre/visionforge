package space.kscience.visionforge.react

import kotlinx.css.*
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

public val ThreeCanvasComponent: FunctionalComponent<ThreeCanvasProps> = functionalComponent(
    "ThreeCanvasComponent"
) { props ->
    val elementRef = useRef<Element>(null)
    var canvas by useState<ThreeCanvas?>(null)

    val three: ThreePlugin = useMemo(props.context){ props.context.fetch(ThreePlugin) }

    useEffect(props.solid, props.options, elementRef) {
        if (canvas == null) {
            val element = elementRef.current as? HTMLElement ?: error("Canvas element not found")
            canvas = ThreeCanvas(three, element, props.options ?: Canvas3DOptions())
        }
    }

    useEffect(canvas, props.solid) {
        props.solid?.let { obj ->
            canvas?.render(obj)
        }
    }

    useEffect(canvas, props.selected) {
        canvas?.select(props.selected)
    }

    styledDiv {
        css {
            maxWidth = 100.vw
            maxHeight = 100.vh
            flex(1.0)
        }
        ref = elementRef
    }
}