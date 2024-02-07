package space.kscience.visionforge.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.dom.clear
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.gather
import space.kscience.dataforge.context.request
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.*


/**
 * Render an Element vision via injected vision renderer inside compose-html
 */
@Composable
public fun Vision(
    context: Context,
    vision: Vision,
    name: Name = "@vision[${vision.hashCode().toString(16)}]".asName(),
    meta: Meta = Meta.EMPTY,
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
): Unit = Div(attrs) {

    val client: VisionClient = context.request(JsVisionClient)

    // set vision root if necessary
    if (vision.manager == null) {
        vision.setAsRoot(client.visionManager)
    }

    val renderer by derivedStateOf {
        client.context.gather<ElementVisionRenderer>(ElementVisionRenderer.TYPE).values.mapNotNull {
            val rating = it.rateVision(vision)
            if (rating > 0) {
                rating to it
            } else {
                null
            }
        }.maxBy { it.first }.second
    }

    DisposableEffect(vision, name, renderer, meta) {
        renderer.render(scopeElement, client, name, vision, meta)
        onDispose {
            scopeElement.clear()
        }
    }
}
