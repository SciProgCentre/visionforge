@file:Suppress("FunctionName")

package three

import three.renderers.WebGLRenderer
import three.renderers.WebGLRendererParams

fun WebGLRenderer(builder: WebGLRendererParams.() -> Unit): WebGLRenderer =
    WebGLRenderer(WebGLRendererParams().apply(builder))