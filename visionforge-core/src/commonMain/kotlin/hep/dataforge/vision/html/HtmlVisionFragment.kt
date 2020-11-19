package hep.dataforge.vision.html

import hep.dataforge.vision.Vision

public class HtmlVisionFragment<V : Vision>(public val layout: HtmlOutputScope<out Any, V>.() -> Unit)

public fun buildVisionFragment(visit: HtmlOutputScope<out Any, Vision>.() -> Unit): HtmlVisionFragment<Vision> =
    HtmlVisionFragment(visit)
