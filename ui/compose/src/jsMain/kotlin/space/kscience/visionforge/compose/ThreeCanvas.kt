package space.kscience.visionforge.compose

import androidx.compose.runtime.*
import kotlinx.dom.clear
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.request
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import space.kscience.visionforge.solid.three.ThreeCanvas
import space.kscience.visionforge.solid.three.ThreePlugin

@Composable
public fun ThreeCanvas(
    context: Context,
    options: Canvas3DOptions?,
    solid: Solid?,
    selected: Name?,
) {

    val three: ThreePlugin by derivedStateOf { context.request(ThreePlugin) }

    Div({
        style {
            maxWidth(100.vw)
            maxHeight(100.vh)
            width(100.percent)
            height(100.percent)
        }
    }) {
        var canvas: ThreeCanvas? = null
        DisposableEffect(options) {
            canvas = ThreeCanvas(three, scopeElement, options ?: Canvas3DOptions())
            onDispose {
                scopeElement.clear()
                canvas = null
            }
        }
        LaunchedEffect(solid) {
            if (solid != null) {
                canvas?.render(solid)
            } else {
                canvas?.clear()
            }
        }
        LaunchedEffect(selected) {
            canvas?.select(selected)
        }
    }
}