package space.kscience.visionforge.html

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.map
import space.kscience.visionforge.Vision
import space.kscience.visionforge.flowProperty
import kotlin.reflect.KProperty1


@Composable
public fun <V : Vision, T> V.collectPropertyAsState(
    property: KProperty1<V, T>,
    propertyName: String = property.name,
): State<T> = flowProperty(propertyName)
    .map { property.get(this@collectPropertyAsState) }
    .collectAsState(property.get(this))