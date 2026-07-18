package space.kscience.visionforge.tables

import js.objects.unsafeJso
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import space.kscience.dataforge.context.AbstractPlugin
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.toDynamic
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.html.ElementVisionRenderer
import space.kscience.visionforge.html.JsVisionClient
import tabulator.Options
import tabulator.TabulatorFull
import tabulator.TabulatorSimpleCss

public class TableVisionJsPlugin : AbstractPlugin(), ElementVisionRenderer {
    public val visionClient: JsVisionClient by require(JsVisionClient)
    public val tablesBase: TableVisionPlugin by require(TableVisionPlugin)

    override val tag: PluginTag get() = Companion.tag

    override fun attach(context: Context) {
        super.attach(context)
        //TODO add dynamic CSS loading
        @Suppress("UnusedVariable") val css = TabulatorSimpleCss
    }

    override fun rateVision(vision: Vision): Int = when (vision) {
        is VisionOfTable -> ElementVisionRenderer.DEFAULT_RATING
        else -> ElementVisionRenderer.ZERO_RATING
    }

    override fun render(element: Element, name: Name, vision: Vision, meta: Meta) {
        val table: VisionOfTable = (vision as? VisionOfTable)
            ?: error("VisionOfTable expected but ${vision::class} found")

        val tableOptions = unsafeJso<Options> {
            columns = Array(table.headers.size + 1) {
                if (it == 0) {
                    unsafeJso {
                        field = "@index"
                        title = "#"
                        resizable = false
                    }
                } else {
                    val header = table.headers[it - 1]
                    unsafeJso {
                        field = header.name
                        title = header.properties.title ?: header.name
                        resizable = true
                    }
                }
            }


            data = table.rows.mapIndexed { index, row ->
                val d = row.meta.toDynamic()
                d["@index"] = index
                d
            }.toTypedArray()

            //layout = "fitColumns"

            pagination = true
            paginationSize = 10
            paginationSizeSelector = arrayOf(10, 25, 50, 100)
        }

        TabulatorFull(element as HTMLElement, tableOptions)
    }

    override fun toString(): String = "Table"

    override fun content(target: String): Map<Name, Any> = when (target) {
        ElementVisionRenderer.TYPE -> mapOf(Name.of("table") to this)
        else -> super.content(target)
    }

    public companion object : PluginFactory<TableVisionJsPlugin> {
        override val tag: PluginTag = PluginTag("vision.table.js", PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): TableVisionJsPlugin = TableVisionJsPlugin()
    }
}