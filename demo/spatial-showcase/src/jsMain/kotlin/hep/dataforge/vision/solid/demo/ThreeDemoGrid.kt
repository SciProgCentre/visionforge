package hep.dataforge.vision.solid.demo

import hep.dataforge.context.Global
import hep.dataforge.meta.Meta
import hep.dataforge.meta.get
import hep.dataforge.meta.string
import hep.dataforge.names.Name
import hep.dataforge.output.Renderer
import hep.dataforge.vision.Vision
import hep.dataforge.vision.solid.three.ThreeCanvas
import hep.dataforge.vision.solid.three.ThreePlugin
import hep.dataforge.vision.solid.three.attachRenderer
import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.h2
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.span
import org.w3c.dom.Element
import kotlin.reflect.KClass

class ThreeDemoGrid(element: Element, meta: Meta = Meta.EMPTY) {

    private val gridRoot = document.create.div("row")
    private val outputs: MutableMap<Name, ThreeCanvas> = HashMap()

    private val three = Global.plugins.fetch(ThreePlugin)

    init {
        element.clear()
        element.append(gridRoot)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(type: KClass<out T>, name: Name, stage: Name, meta: Meta): Renderer<T> {

        return outputs.getOrPut(name) {
            if (type != Vision::class) error("Supports only DisplayObject")
            lateinit var output: ThreeCanvas
            //TODO calculate cell width here using jquery
            gridRoot.append {
                span("border") {
                    div("col-6") {
                        div { id = "output-$name" }.also {
                            output = three.attachRenderer(it, canvasOptions)
                            //output.attach(it)
                        }
                        hr()
                        h2 { +(meta["title"].string ?: name.toString()) }
                    }
                }
            }

            output
        } as Renderer<T>
    }
}

