package space.kscience.visionforge.compose

import androidx.compose.runtime.Composable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionClient

public interface ComposeVisionRenderer {
    public fun rateVision(vision: Vision): Int
    @Composable
    public fun render(client: VisionClient, name: Name, vision: Vision, meta: Meta)

    public companion object
}