package hep.dataforge.vision.solid.demo

import hep.dataforge.context.Global
import hep.dataforge.meta.Meta
import hep.dataforge.meta.get
import hep.dataforge.meta.string
import hep.dataforge.names.Name
import hep.dataforge.vision.VisionLayout
import hep.dataforge.vision.solid.Solid
import hep.dataforge.vision.solid.three.ThreeCanvas
import hep.dataforge.vision.solid.three.ThreePlugin
import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.*
import kotlinx.html.style
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class ThreeDemoGrid(element: Element) : VisionLayout<Solid> {
    private lateinit var navigationElement: HTMLElement
    private lateinit var contentElement: HTMLDivElement

    private val outputs: MutableMap<Name, ThreeCanvas> = HashMap()

    private val three = Global.plugins.fetch(ThreePlugin)

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
            three.createCanvas(element, canvasOptions)
        }.render(vision)
    }
}

