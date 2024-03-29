package space.kscience.visionforge.solid.demo

import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.*
import kotlinx.html.style
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.request
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.three.ThreeCanvas
import space.kscience.visionforge.solid.three.ThreePlugin

class ThreeDemoGrid(element: Element) : VisionLayout<Solid> {
    private lateinit var navigationElement: HTMLElement
    private lateinit var contentElement: HTMLDivElement

    private val outputs: MutableMap<Name, ThreeCanvas> = HashMap()

    private val three = Global.request(ThreePlugin)

    override val solids: Solids get() = three.solids

    init {
        element.clear()
        element.append {
            nav("navbar navbar-expand-md navbar-dark fixed-top bg-dark") {
                a(classes = "navbar-brand") {
                    href = "#"
                    +"Demo grid"
                }
                div("navbar-collapse collapse") {
                    id = "navbar"
                    navigationElement = ul("nav navbar-nav")
                }
            }
            contentElement = div {
                id = "content"
            }
        }
    }

    override fun render(name: Name, vision: Solid, meta: Meta) {
        outputs.getOrPut(name) {
            navigationElement.append {
                li("nav-item") {
                    a(classes = "nav-link") {
                        href = "#$name"
                        +name.toString()
                    }
                }
            }
            contentElement.append {
                div("container") {
                    id = name.toString()
                    hr()
                    h2 { +(meta["title"].string ?: name.toString()) }
                    hr()
                    div {
                        style = "height: 600px;"
                        id = "output-$name"
                    }
                }
            }
            val element = document.getElementById("output-$name") ?: error("Element not found")
            ThreeCanvas(three, element, canvasOptions)
        }.render(vision)
    }
}

