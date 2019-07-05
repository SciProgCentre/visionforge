package hep.dataforge.vis.spatial.demo

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.Context
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.meta.Meta
import hep.dataforge.meta.buildMeta
import hep.dataforge.meta.get
import hep.dataforge.meta.string
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.output.Output
import hep.dataforge.output.OutputManager
import hep.dataforge.vis.common.DisplayGroup
import hep.dataforge.vis.common.DisplayObject
import hep.dataforge.vis.spatial.render
import hep.dataforge.vis.spatial.three.ThreeOutput
import hep.dataforge.vis.spatial.three.ThreePlugin
import hep.dataforge.vis.spatial.three.output
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.h2
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.span
import kotlin.browser.document
import kotlin.dom.clear
import kotlin.reflect.KClass

class ThreeDemoGrid(meta: Meta) : AbstractPlugin(meta), OutputManager {
    override val tag: PluginTag get() = Companion.tag

    private val gridRoot = document.create.div("row")
    private val outputs: MutableMap<Name, ThreeOutput> = HashMap()

    override fun dependsOn(): List<PluginFactory<*>> = listOf(ThreePlugin)

    override fun attach(context: Context) {
        super.attach(context)
        val elementId = meta["elementID"].string ?: "canvas"
        val element = document.getElementById(elementId) ?: error("Element with id $elementId not found on page")
        element.clear()
        element.append(gridRoot)
    }

    override fun <T : Any> get(type: KClass<out T>, name: Name, stage: Name, meta: Meta): Output<T> {
        val three = context.plugins.get<ThreePlugin>()!!

        return outputs.getOrPut(name) {
            if (type != DisplayObject::class) error("Supports only DisplayObject")
            val output = three.output(meta) {
                "axis" to {
                    "size" to 500
                }
            }
            //TODO calculate cell width here using jquery
            gridRoot.append {
                span("border") {
                    div("col-6") {
                        output.attach(div { id = "output-$name" }) { 500 }
                        hr()
                        h2 { +(meta["title"].string ?: name.toString()) }
                    }
                }
            }

            output
        } as Output<T>
    }

    companion object : PluginFactory<ThreeDemoGrid> {
        override val tag: PluginTag = PluginTag(group = "hep.dataforge", name = "vis.js.spatial.demo")

        override val type: KClass<out ThreeDemoGrid> = ThreeDemoGrid::class

        override fun invoke(meta: Meta): ThreeDemoGrid = ThreeDemoGrid(meta)
    }
}

fun ThreeDemoGrid.demo(name: String, title: String = name, block: DisplayGroup.() -> Unit) {
    val meta = buildMeta {
        "title" to title
    }
    val output = get<DisplayObject>(DisplayObject::class, name.toName(), meta = meta)
    output.render(action = block)
}