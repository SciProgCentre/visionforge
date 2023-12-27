package space.kscience.visionforge.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.dom.clear
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement
import space.kscience.dataforge.context.gather
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.ElementVisionRenderer
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionClient


/**
 * Render an Element vision via injected vision renderer inside compose-html
 */
@Composable
public fun Vision(
    client: VisionClient,
    vision: Vision,
    name: Name = "@vision[${vision.hashCode().toString(16)}]".asName(),
    meta: Meta = Meta.EMPTY,
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
): Unit = Div(attrs) {

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
