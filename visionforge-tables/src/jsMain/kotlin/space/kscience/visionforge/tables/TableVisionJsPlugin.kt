package space.kscience.visionforge.tables

import js.core.jso
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import space.kscience.dataforge.context.AbstractPlugin
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.toDynamic
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.ElementVisionRenderer
import space.kscience.visionforge.JsVisionClient
import space.kscience.visionforge.Vision
import tabulator.Tabulator
import tabulator.TabulatorFull

public class TableVisionJsPlugin : AbstractPlugin(), ElementVisionRenderer {
    public val visionClient: JsVisionClient by require(JsVisionClient)
    public val tablesBase: TableVisionPlugin by require(TableVisionPlugin)

    override val tag: PluginTag get() = Companion.tag

    override fun attach(context: Context) {
        super.attach(context)
        kotlinext.js.require<Any>("tabulator-tables/dist/css/tabulator.min.css")
        kotlinext.js.require<Any>("tabulator-tables/src/js/modules/ResizeColumns/ResizeColumns.js")
    }

    override fun rateVision(vision: Vision): Int = when (vision) {
        is VisionOfTable -> ElementVisionRenderer.DEFAULT_RATING
        else -> ElementVisionRenderer.ZERO_RATING
    }

    override fun render(element: Element, name: Name, vision: Vision, meta: Meta) {
        val table: VisionOfTable = (vision as? VisionOfTable)
            ?: error("VisionOfTable expected but ${vision::class} found")

        val tableOptions = jso<Tabulator.Options> {
            columns = table.headers.map { header ->
                jso<Tabulator.ColumnDefinition> {
                    field = header.name
                    title = header.properties.title ?: header.name
                    resizable = true
                }
            }.toTypedArray()

            columns = Array(table.headers.size + 1) {
                if (it == 0) {
                    jso {
                        field = "@index"
                        title = "#"
                        resizable = false
                    }
                } else {
                    val header = table.headers[it - 1]
                    jso {
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

    override fun content(target: String): Map<Name, Any> = when (target) {
        ElementVisionRenderer.TYPE -> mapOf("table".asName() to this)
        else -> super.content(target)
    }

    public companion object : PluginFactory<TableVisionJsPlugin> {
        override val tag: PluginTag = PluginTag("vision.table.js", PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): TableVisionJsPlugin = TableVisionJsPlugin()
    }
}