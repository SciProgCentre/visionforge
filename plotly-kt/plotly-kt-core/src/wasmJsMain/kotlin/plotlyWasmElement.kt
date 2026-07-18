@file:OptIn(ExperimentalWasmJsInterop::class)

package space.kscience.plotly

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.html.TagConsumer
import kotlinx.html.div
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.w3c.dom.Element
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import org.w3c.dom.MutationRecord
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.meta.MetaSerializer
import space.kscience.dataforge.meta.toJson
import space.kscience.plotly.Plotly.coordinateNames
import space.kscience.plotly.models.Trace
import space.kscience.visionforge.VisionGroupCompositionChangedEvent
import space.kscience.visionforge.VisionPropertyChangedEvent

@JsFun("s => JSON.parse(s)")
private external fun json(s: String): JsAny

private fun JsonElement.toWasmJs(): JsAny {
    val string = toString()
    return json(string)
}

@OptIn(ExperimentalSerializationApi::class)
private fun MetaRepr.toWasmJs(): JsAny = Json.encodeToJsonElement(MetaSerializer, toMeta()).toWasmJs()

private fun List<MetaRepr>.toWasmJs(): JsArray<JsAny> = map { it.toWasmJs() }.toJsArray()

@Suppress("UNUSED_PARAMETER")
private fun myMutationObserverInit(
    childList: Boolean?,
    attributes: Boolean?,
): MutationObserverInit = js("({ childList: childList, attributes: attributes})")


/**
 * Attach a plot to this element or update the existing plot
 */
@OptIn(DelicateCoroutinesApi::class)
public fun Element.plot(
    plotlyConfig: PlotlyConfig,
    plot: Plot,
    scope: CoroutineScope = plot.manager?.context ?: GlobalScope
) {
    //send initial data
    PlotlyWasm.react(
        graphDiv = this,
        data = plot.data.toWasmJs(),
        layout = plot.layout.toWasmJs(),
        config = plotlyConfig.toWasmJs()
    )

    //start updates
    val listenJob = scope.launch {
        plot.data.forEachIndexed { index, trace: Trace ->
            trace.eventFlow.filterIsInstance<VisionPropertyChangedEvent>().onEach { event ->

                val traceMeta = trace.toMeta()

                //wrap coordinates into an additional array because plotly API for some reason expects 2D arrays
                val traceJson = JsonObject(
                    traceMeta.items.map { (token, item) ->
                        val key = token.toStringUnescaped()
                        val valueUnwrapped = item.toJson()
                        val value = if (key in coordinateNames) JsonArray(listOf(valueUnwrapped)) else valueUnwrapped
                        key to value
                    }.toMap()
                )

                PlotlyWasm.restyle(this@plot, traceJson.toWasmJs(), listOf(index.toJsNumber()).toJsArray())
            }.launchIn(this)
        }

        plot.eventFlow.onEach { event ->
            when (event) {
                is VisionGroupCompositionChangedEvent -> PlotlyWasm.react(this@plot, plot.data.toWasmJs())
                is VisionPropertyChangedEvent -> PlotlyWasm.relayout(this@plot, plot.layout.toWasmJs())
                else -> {
                    //ignore
                }
            }
        }.launchIn(this)
    }

    //observe node removal to avoid memory leak
    MutationObserver { records: JsArray<MutationRecord>, _ ->
        if (records.toList().firstOrNull()?.removedNodes?.length != 0) {
            listenJob.cancel()
        }
    }.observe(this, myMutationObserverInit(childList = true, attributes = false))
}

@Deprecated("Change arguments positions", ReplaceWith("plot(plotlyConfig, plot)"))
public fun Element.plot(plot: Plot, plotlyConfig: PlotlyConfig = PlotlyConfig()): Unit = plot(plotlyConfig, plot)

/**
 * Create a plot in this element
 */
public inline fun Element.plot(
    scope: CoroutineScope,
    plotlyConfig: PlotlyConfig = PlotlyConfig(),
    plotBuilder: Plot.() -> Unit
) {
    plot(plotlyConfig, Plot().apply(plotBuilder), scope)
}

public class PlotlyElement(public val div: Element)

/**
 * Create a div element and render the plot in it
 */
@OptIn(DelicateCoroutinesApi::class)
public fun TagConsumer<Element>.plotDiv(
    plot: Plot,
    plotlyConfig: PlotlyConfig = PlotlyConfig(),
    scope: CoroutineScope = plot.manager?.context ?: GlobalScope,
): PlotlyElement = PlotlyElement(div("plotly-kt-plot").apply { plot(plotlyConfig, plot) })

/**
 * Render plot in the HTML element using direct plotly API.
 */
public inline fun TagConsumer<Element>.plotDiv(
    scope: CoroutineScope,
    plotlyConfig: PlotlyConfig = PlotlyConfig(),
    plotBuilder: Plot.() -> Unit,
): PlotlyElement = PlotlyElement(div("plotly-kt-plot").apply { plot(scope, plotlyConfig, plotBuilder) })

// TODO implement events
//@OptIn(ExperimentalSerializationApi::class)
//public fun PlotlyElement.on(eventType: PlotlyEventListenerType, block: MouseEvent.(PlotlyEvent) -> Unit) {
//    div.addEventListener(eventType.eventType) { event: Event ->
//        val eventData = PlotlyEvent(event.points.map {
//            PlotlyEventPoint(
//                curveNumber = it.curveNumber as Int,
//                pointNumber = it.pointNumber as? Int,
//                x = Value.of(it.x),
//                y = Value.of(it.y),
//                data = Json.decodeFromDynamic(it.data)
//            )
//        })
//        event.event.block(eventData)
//    }
//}
