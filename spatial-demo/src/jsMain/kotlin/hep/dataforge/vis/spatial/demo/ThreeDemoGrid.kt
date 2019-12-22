package hep.dataforge.vis.spatial.demo

import hep.dataforge.context.Global
import hep.dataforge.meta.Meta
import hep.dataforge.meta.get
import hep.dataforge.meta.string
import hep.dataforge.names.Name
import hep.dataforge.output.OutputManager
import hep.dataforge.output.Renderer
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.spatial.three.ThreeCanvas
import hep.dataforge.vis.spatial.three.ThreePlugin
import hep.dataforge.vis.spatial.three.output
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.h2
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.span
import org.w3c.dom.Element
import kotlin.browser.document
import kotlin.dom.clear
import kotlin.reflect.KClass

class ThreeDemoGrid(element: Element, meta: Meta = Meta.empty) : OutputManager {

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
            if (type != VisualObject::class) error("Supports only DisplayObject")
            val output = three.output(meta = meta) {
                "minSize" put 500
                "axis" put {
                    "size" put 500
                }
            }
            //TODO calculate cell width here using jquery
            gridRoot.append {
                span("border") {
                    div("col-6") {
                        div { id = "output-$name" }.also {
                            output.attach(it)
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

