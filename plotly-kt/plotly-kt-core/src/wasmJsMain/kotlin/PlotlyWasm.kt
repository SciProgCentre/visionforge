@file:OptIn(ExperimentalWasmJsInterop::class)

package space.kscience.plotly

import org.w3c.dom.Element
import org.w3c.dom.events.MouseEvent
import kotlin.js.Promise

public external interface ToImgOpts {
    public var format: JsString /* 'jpeg' | 'png' | 'webp' | 'svg' */
    public var width: JsNumber
    public var height: JsNumber
}

public external interface DownloadImgOpts {
    public var format: JsString /* 'jpeg' | 'png' | 'webp' | 'svg' */
    public var width: JsNumber
    public var height: JsNumber
    public var filename: JsString
}


@JsName("Plotly")
@JsModule("plotly.js/dist/plotly.js")
public external object PlotlyWasm {
    public fun newPlot(
        graphDiv: Element,
        data: JsArray<JsAny> = definedExternally,
        layout: JsAny = definedExternally,
        config: JsAny = definedExternally
    )

    public fun react(
        graphDiv: Element,
        data: JsArray<JsAny> = definedExternally,
        layout: JsAny = definedExternally,
        config: JsAny = definedExternally
    )

    public fun update(
        graphDiv: Element,
        data: JsAny = definedExternally,
        layout: JsAny = definedExternally
    )

    public fun restyle(graphDiv: Element, update: JsAny, traceIndices: JsArray<JsNumber>? = definedExternally)
    public fun relayout(graphDiv: Element, update: JsAny)

    public fun toImage(root: Element, opts: ToImgOpts): Promise<JsString>
    public fun downloadImage(root: Element, opts: DownloadImgOpts): Promise<JsString>
}


public external interface PlotMouseEvent {
    public val points: JsArray<JsAny>
    public val event: MouseEvent
}
