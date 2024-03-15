package space.kscience.visionforge.compose

import androidx.compose.runtime.Composable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.misc.DfType
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import kotlin.reflect.KClass
import kotlin.reflect.cast

@DfType(ComposeVisionRenderer.TYPE)
public interface ComposeVisionRenderer {
    public fun rateVision(vision: Vision): Int

    @Composable
    public fun render(name: Name, vision: Vision, meta: Meta)

    public companion object {
        public const val TYPE: String = "composeVisionRenderer"
        public const val ZERO_RATING: Int = 0
        public const val DEFAULT_RATING: Int = 10
    }
}

public class SingleTypeComposeRenderer<T : Vision>(
    public val kClass: KClass<T>,
    private val acceptRating: Int = ComposeVisionRenderer.DEFAULT_RATING,
    private val renderFunction: @Composable (name: Name, vision: T, meta: Meta) -> Unit,
) : ComposeVisionRenderer {

    override fun rateVision(vision: Vision): Int =
        if (vision::class == kClass) acceptRating else ComposeVisionRenderer.ZERO_RATING

    @Composable
    override fun render(
        name: Name,
        vision: Vision,
        meta: Meta,
    ) {
        renderFunction(name, kClass.cast(vision), meta)
    }
}

public inline fun <reified T : Vision> ComposeVisionRenderer(
    acceptRating: Int = ComposeVisionRenderer.DEFAULT_RATING,
    noinline renderFunction: @Composable (name: Name, vision: T, meta: Meta) -> Unit,
): ComposeVisionRenderer = SingleTypeComposeRenderer(T::class, acceptRating, renderFunction)
