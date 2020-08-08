@file:Suppress("FunctionName")

package info.laht.threekt

import info.laht.threekt.renderers.WebGLRenderer
import info.laht.threekt.renderers.WebGLRendererParams

fun WebGLRenderer(builder: WebGLRendererParams.() -> Unit): WebGLRenderer =
    WebGLRenderer(WebGLRendererParams().apply(builder))