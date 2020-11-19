package hep.dataforge.vision.solid.demo

import hep.dataforge.context.Global
import hep.dataforge.meta.Meta
import hep.dataforge.meta.get
import hep.dataforge.meta.string
import hep.dataforge.names.Name
import hep.dataforge.vision.layout.Output
import hep.dataforge.vision.layout.Page
import hep.dataforge.vision.solid.Solid
import hep.dataforge.vision.solid.three.ThreeCanvas
import hep.dataforge.vision.solid.three.ThreePlugin
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.*
import kotlinx.html.role
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class ThreeDemoGrid(element: Element, idPrefix: String = "") : Page<Solid> {


    private lateinit var navigationElement: HTMLElement
    private lateinit var contentElement: HTMLDivElement

    private val outputs: MutableMap<Name, ThreeCanvas> = HashMap()

    private val three = Global.plugins.fetch(ThreePlugin)

    init {
        element.clear()
        element.append {
            div("container") {
                navigationElement = ul("nav nav-tabs") {
                    id = "${idPrefix}Tab"
                    role = "tablist"
                }
                contentElement = div("tab-content") {
                    id = "${idPrefix}TabContent"
                }
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    override fun output(name: Name, meta: Meta): Output<Solid> = outputs.getOrPut(name) {
        lateinit var output: ThreeCanvas
        navigationElement.append {
            li("nav-item") {
                a(classes = "nav-link") {
                    id = "tab[$name]"
                    attributes["data-toggle"] = "tab"
                    href = "#$name"
                    role = "tab"
                    attributes["aria-controls"] = "$name"
                    attributes["aria-selected"] = "false"
                    +name.toString()
                }
            }
        }
        contentElement.append {
            div("tab-pane fade col h-100") {
                id = name.toString()
                role = "tabpanel"
                attributes["aria-labelledby"] = "tab[$name]"
                div("container w-100 h-100") { id = "output-$name" }.also {element->
                    output = three.createCanvas(element, canvasOptions)
                }
                hr()
                h2 { +(meta["title"].string ?: name.toString()) }
            }
        }
        output
    }
}

