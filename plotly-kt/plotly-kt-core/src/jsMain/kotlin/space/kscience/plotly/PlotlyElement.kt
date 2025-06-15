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
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import org.w3c.dom.*
import org.w3c.dom.events.MouseEvent
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.meta.MetaSerializer
import space.kscience.dataforge.meta.Value
import space.kscience.plotly.events.PlotlyEvent
import space.kscience.plotly.events.PlotlyEventListenerType
import space.kscience.plotly.events.PlotlyEventPoint
import space.kscience.visionforge.VisionGroupCompositionChangedEvent
import space.kscience.visionforge.VisionPropertyChangedEvent

@OptIn(ExperimentalSerializationApi::class)
private fun MetaRepr.toDynamic(): dynamic = Json.encodeToDynamic(MetaSerializer, toMeta())

private fun List<MetaRepr>.toDynamic(): Array<dynamic> = map { it.toDynamic() }.toTypedArray()

/**
 * Attach a plot to this element or update the existing plot
 */
@OptIn(DelicateCoroutinesApi::class)
public fun Element.plot(
    plotlyConfig: PlotlyConfig,
    plot: Plot,
    scope: CoroutineScope = plot.manager?.context ?: GlobalScope
) {

//    console.info("""
//                        Plotly.react(
//                            '$this',
//                            ${JSON.stringify(tracesData)},
//                            ${JSON.stringify(layout)}
//                        );
//                    """.trimIndent())

    //send initial data
    PlotlyJs.react(
        graphDiv = this,
        data = plot.data.toDynamic(),
        layout = plot.layout.toDynamic(),
        config = plotlyConfig.toDynamic()
    )

    //start updates
    val listenJob = scope.launch {
        plot.data.forEachIndexed { index, trace ->
            trace.eventFlow.filterIsInstance<VisionPropertyChangedEvent>().onEach { event ->
                val traceData = trace.toDynamic()

                Plotly.coordinateNames.forEach { coordinate ->
                    val data = traceData[coordinate]
                    if (traceData[coordinate] != null) {
                        traceData[coordinate] = arrayOf(data)
                    }
                }

                PlotlyJs.restyle(this@plot, traceData, arrayOf(index))
            }.launchIn(this)
        }

        plot.eventFlow.onEach { event ->
            when (event) {
                is VisionGroupCompositionChangedEvent -> PlotlyJs.restyle(this@plot, plot.data.toDynamic())
                is VisionPropertyChangedEvent -> PlotlyJs.relayout(this@plot, plot.layout.toDynamic())
                else -> {
                    //ignore
                }
            }
        }.launchIn(this)
    }

    //observe node removal to avoid memory leak
    MutationObserver { records: Array<MutationRecord>, _ ->
        if (records.firstOrNull()?.removedNodes?.length != 0) {
            listenJob.cancel()
        }
    }.observe(this, MutationObserverInit(childList = true, attributes = false))
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

public class PlotlyElement(public val div: HTMLElement)

/**
 * Create a div element and render the plot in it
 */
@OptIn(DelicateCoroutinesApi::class)
public fun TagConsumer<HTMLElement>.plotDiv(
    plotlyConfig: PlotlyConfig,
    plot: Plot,
    scope: CoroutineScope = plot.manager?.context ?: GlobalScope,
): PlotlyElement = PlotlyElement(div("plotly-kt-plot").apply { plot(plotlyConfig, plot) })

/**
 * Render plot in the HTML element using direct plotly API.
 */
public inline fun TagConsumer<HTMLElement>.plotDiv(
    scope: CoroutineScope,
    plotlyConfig: PlotlyConfig = PlotlyConfig(),
    plotBuilder: Plot.() -> Unit,
): PlotlyElement = PlotlyElement(div("plotly-kt-plot").apply { plot(scope, plotlyConfig, plotBuilder) })

@OptIn(ExperimentalSerializationApi::class)
public fun PlotlyElement.on(eventType: PlotlyEventListenerType, block: MouseEvent.(PlotlyEvent) -> Unit) {
    div.asDynamic().on(eventType.eventType) { event: PlotMouseEvent ->
        val eventData = PlotlyEvent(event.points.map {
            PlotlyEventPoint(
                curveNumber = it.curveNumber as Int,
                pointNumber = it.pointNumber as? Int,
                x = Value.of(it.x),
                y = Value.of(it.y),
                data = Json.decodeFromDynamic(it.data)
            )
        })
        event.event.block(eventData)
    }
}
