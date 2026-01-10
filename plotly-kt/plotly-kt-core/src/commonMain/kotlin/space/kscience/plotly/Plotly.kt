package space.kscience.plotly

import kotlinx.html.TagConsumer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.context.request
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.plotly.models.Trace
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.html.VisionOutput
import space.kscience.visionforge.html.VisionTagConsumer
import kotlin.js.JsName

/**
 * A namespace for utility functions
 */
@JsName("PlotlyKt")
public object Plotly : ContextAware {
    public const val VERSION: String = "2.35.3"

    public const val PLOTLY_CDN: String = "https://cdn.plot.ly/plotly-${VERSION}.min.js"
    //"https://cdnjs.cloudflare.com/ajax/libs/plotly.js/${VERSION}/plotly.min.js"

    public val coordinateNames: List<String> = listOf(
        "x", "y", "z", "text", "hovertext", "close", "high", "low", "open", "locations", "lon", "lat", "ids"
    )

    override val context: Context by lazy {
        Context("Plotly") {
            plugin(PlotlyPlugin)
        }
    }

    public val plugin: PlotlyPlugin by lazy { context.request(PlotlyPlugin) }

    public inline fun plot(block: Plot.() -> Unit): Plot = Plot().apply(block)
}

/**
 * Convert any type-safe configurator to json string
 */
public fun Scheme.toJsonString(): String = meta.toJson().toString()

private fun List<Trace>.toJson(): JsonArray = buildJsonArray {
    forEach { add(it.properties.toJson()) }
}

/**
 * Convert list of type-safe configurators to json array string
 */
public fun List<Trace>.toJsonString(): String = toJson().toString()

@RequiresOptIn("Unstable API subjected to change in future releases", RequiresOptIn.Level.WARNING)
public annotation class UnstablePlotlyAPI

@RequiresOptIn("This Plotly JS API is not fully supported. Use it at your own risk.", RequiresOptIn.Level.ERROR)
public annotation class UnsupportedPlotlyAPI

public class PlotlyConfig : Scheme() {
    public var editable: Boolean? by boolean()
    public var showEditInChartStudio: Boolean? by boolean()
    public var plotlyServerURL: String? by string()
    public var responsive: Boolean? by boolean()
    public var imageFormat: String? by string(Name.parse("toImageButtonOptions.format"))

    /**
     * A list of class names applied to the output `div` in the generated HTML for the plot.
     *
     * This property allows customization of the CSS classes assigned to the `div` element
     * that contains the rendered plot. It can be utilized to add custom styling or specific
     * class-based behaviors to the output.
     *
     * By default, this property is initialized as an empty list and can be updated to include
     * necessary class names as strings.
     */
    public var classes: List<String> by stringList(default = emptyArray(), key = VisionTagConsumer.OUTPUT_DIV_CLASSES_KEY.asName())

    public fun withEditorButton() {
        showEditInChartStudio = true
        plotlyServerURL = "https://chart-studio.plotly.com"
    }

    public fun saveAsSvg() {
        imageFormat = "svg"
    }

    override fun toString(): String = toJsonString()

    public companion object : SchemeSpec<PlotlyConfig>(::PlotlyConfig)
}

/**
 * Embed a dynamic plotly plot in a vision
 */
@VisionBuilder
public inline fun VisionOutput.plotly(
    config: PlotlyConfig = PlotlyConfig(),
    block: Plot.() -> Unit,
): Plot {
    requirePlugin(PlotlyPlugin)
    meta = config.meta
    return Plotly.plot(block)
}


//FIXME rework VisionTagConsumer toa a context
context(rootConsumer: VisionTagConsumer<*>)
public fun TagConsumer<*>.plot(
    config: PlotlyConfig = PlotlyConfig(),
    block: Plot.() -> Unit,
): Unit = with(rootConsumer) {
    this@plot.vision {
        plotly(config, block)
    }
}