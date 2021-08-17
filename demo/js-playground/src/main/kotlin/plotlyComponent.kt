import kotlinx.css.*
import kotlinx.css.properties.border
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import react.*
import space.kscience.plotly.Plot
import space.kscience.plotly.PlotlyConfig
import space.kscience.plotly.plot
import styled.css
import styled.styledDiv

external interface PlotlyProps : RProps {
    var plot: Plot?
}


val Plotly = functionalComponent<PlotlyProps>("Plotly") { props ->
    val elementRef = useRef<Element>(null)

    useEffect(props.plot, elementRef) {
        val element = elementRef.current as? HTMLElement ?: error("Plotly element not found")
        props.plot?.let {
            element.plot(it, PlotlyConfig {
                responsive = true
            })
        }
    }

    styledDiv {
        css {
            width = 100.pct
            height = 100.pct
            border(2.pt, BorderStyle.solid, Color.blue)
            flex(1.0)
        }
        ref = elementRef
    }
}

fun RBuilder.plotly(plotbuilder: Plot.() -> Unit) = Plotly {
    attrs {
        this.plot = Plot().apply(plotbuilder)
    }
}