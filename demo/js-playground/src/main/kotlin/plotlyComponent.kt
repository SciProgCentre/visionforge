import kotlinx.css.*
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import react.RProps
import react.functionalComponent
import react.useEffect
import react.useRef
import space.kscience.dataforge.context.Context
import space.kscience.plotly.Plot
import space.kscience.plotly.plot
import styled.css
import styled.styledDiv

external interface PlotlyProps: RProps{
    var context: Context
    var plot: Plot?
}


val Plotly = functionalComponent<PlotlyProps>("Plotly"){props ->
    val elementRef = useRef<Element>(null)

    useEffect(props.plot, elementRef) {
        val element = elementRef.current as? HTMLElement ?: error("Plotly element not found")
        props.plot?.let { element.plot(it)}
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