import kotlinx.css.*
import kotlinx.css.properties.border
import kotlinx.dom.clear
import kotlinx.html.dom.append
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import react.RProps
import react.functionalComponent
import react.useEffect
import react.useRef
import space.kscience.visionforge.markup.VisionOfMarkup
import space.kscience.visionforge.markup.markdown
import space.kscience.visionforge.useProperty
import styled.css
import styled.styledDiv

external interface MarkupProps : RProps {
    var markup: VisionOfMarkup?
}

val Markup = functionalComponent<MarkupProps>("Markup") { props ->
    val elementRef = useRef<Element>(null)

    useEffect(props.markup, elementRef) {
        val element = elementRef.current as? HTMLElement ?: error("Markup element not found")
        props.markup?.let { vision ->
            val flavour = when (vision.format) {
                VisionOfMarkup.COMMONMARK_FORMAT -> CommonMarkFlavourDescriptor()
                VisionOfMarkup.GFM_FORMAT -> GFMFlavourDescriptor()
                //TODO add new formats via plugins
                else -> error("Format ${vision.format} not recognized")
            }
            vision.useProperty(VisionOfMarkup::content) { content ->
                element.clear()
                element.append {
                    markdown(flavour) { content ?: "" }
                }
            }
        }
    }

    styledDiv {
        css {
            width = 100.pct
            height = 100.pct
            border(2.pt, BorderStyle.solid, Color.blue)
            padding(left = 8.pt)
            backgroundColor = Color.white
            flex(1.0)
            zIndex = 10000
        }
        ref = elementRef
    }
}