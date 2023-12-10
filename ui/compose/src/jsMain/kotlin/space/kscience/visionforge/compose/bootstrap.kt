package space.kscience.visionforge.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.H5
import org.jetbrains.compose.web.dom.Text

@Composable
public fun CardTitle(title: String): Unit = H5({ classes("card-title") }) { Text(title) }