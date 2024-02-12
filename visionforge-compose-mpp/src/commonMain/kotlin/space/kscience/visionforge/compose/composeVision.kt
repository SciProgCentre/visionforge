package space.kscience.visionforge.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.Vision


/**
 * Render an Element vision via injected vision renderer inside compose-html
 */
@Composable
public fun Vision(
    context: Context,
    vision: Vision,
    name: Name = "@vision[${vision.hashCode().toString(16)}]".asName(),
    meta: Meta = Meta.EMPTY,
    modifier: Modifier = Modifier,
) {

}
