package space.kscience.plotly

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.node
import space.kscience.dataforge.names.NameToken
import space.kscience.plotly.models.Layout
import space.kscience.plotly.models.Trace
import space.kscience.visionforge.*

/**
 * The main plot class.
 *
 */
@Serializable
public class Plot : AbstractVision(), MutableVisionGroup<Trace> {

    @SerialName("data")
    private val _data = mutableListOf<Trace>()
    public val data: List<Trace> get() = _data

    override val visions: Map<NameToken, Trace>
        get() = data.mapIndexed { index, value -> NameToken("trace", index.toString()) to value }.toMap()

    override fun convertVisionOrNull(vision: Vision): Trace? = vision as? Trace

    @JvmSynchronized
    override fun setVision(
        token: NameToken,
        vision: Trace?
    ) {
        val index = if (token.body == "trace") token.index?.toIntOrNull() ?: -1 else -1
        if (index != -1) {
            if (vision == null) {
                _data.removeAt(index)
            } else {
                _data[index] = vision
            }
        } else {
            if (vision != null) {
                _data.add(vision)
            }
        }
    }

    /**
     * Layout specification for th plot
     */
    public val layout: Layout by properties.scheme(Layout)

    @JvmSynchronized
    public fun addTrace(trace: Trace) {
        _data.add(trace)
        emitEvent(VisionGroupCompositionChangedEvent(NameToken("trace", _data.size.toString()), trace))
    }

    /**
     * Append all traces from [traces] to the plot
     */
    public fun traces(traces: Collection<Trace>) {
        traces.forEach { addTrace(it) }
    }

    /**
     * Append all [traces]
     */
    public fun traces(vararg traces: Trace) {
        traces.forEach { addTrace(it) }
    }

    /**
     * Remove a trace with a given [index] from the plot
     */
    @UnstablePlotlyAPI
    @JvmSynchronized
    public fun removeTrace(index: Int) {
        _data.removeAt(index)
        emitEvent(VisionGroupCompositionChangedEvent(NameToken("trace", _data.size.toString()), null))
    }

    override val descriptor: MetaDescriptor get() = Companion.descriptor

    public companion object {
        public val descriptor: MetaDescriptor = MetaDescriptor {
            node(Plot::data.name, Trace) {
                multiple = true
            }

            node(Plot::layout.name, Layout)
        }
    }
}

public fun Plot(meta: Meta): Plot = Plot().apply {
    meta["layout"]?.let { layoutMeta -> layout { update(layoutMeta) } }
    meta.getIndexed("data").forEach { (_, data) ->
        addTrace(Trace().apply { update(data) })
    }
}

internal fun Plot.toJson(): JsonObject = buildJsonObject {
    put("layout", layout.meta.toJson())
    put("data", buildJsonArray {
        data.forEach { traceData ->
            add(traceData.properties.toJson())
        }
    })
}

/**
 * Convert a plot to Json representation specified by Plotly `newPlot` command.
 */
public fun Plot.toJsonString(): String = toJson().toString()

/**
 * Configure the layout
 */
public inline fun Plot.layout(block: Layout.() -> Unit) {
    layout.invoke(block)
}

/**
 * Add a generic trace
 */
public inline fun Plot.trace(block: Trace.() -> Unit): Trace {
    val trace = Trace().apply(block)
    traces(trace)
    return trace
}