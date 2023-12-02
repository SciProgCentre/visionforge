package space.kscience.visionforge

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.parseAsName
import space.kscience.dataforge.names.startsWith
import kotlin.reflect.KProperty1


/**
 * Call [callback] on initial value of the property and then on all subsequent values after change
 */
public fun Vision.useProperty(
    propertyName: Name,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
    scope: CoroutineScope? = manager?.context,
    callback: (Meta) -> Unit,
): Job {
    //Pass initial value.
    callback(properties.getMeta(propertyName, inherit, includeStyles))
    return properties.changes.onEach { name ->
        if (name.startsWith(propertyName)) {
            callback(properties.getMeta(propertyName, inherit, includeStyles))
        }
    }.launchIn(scope ?: error("Orphan Vision can't observe properties"))
}

public fun Vision.useProperty(
    propertyName: String,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
    scope: CoroutineScope? = manager?.context,
    callback: (Meta) -> Unit,
): Job = useProperty(propertyName.parseAsName(), inherit, includeStyles, scope, callback)

/**
 * Observe changes to the specific property without passing the initial value.
 */
public fun <V : Vision, T> V.onPropertyChange(
    property: KProperty1<V, T>,
    scope: CoroutineScope = manager?.context ?: error("Orphan Vision can't observe properties"),
    callback: suspend V.(T) -> Unit,
): Job = properties.changes.onEach { name ->
    if (name.startsWith(property.name.asName())) {
        callback(property.get(this))
    }
}.launchIn(scope)

public fun <V : Vision, T> V.useProperty(
    property: KProperty1<V, T>,
    scope: CoroutineScope = manager?.context ?: error("Orphan Vision can't observe properties"),
    callback: V.(T) -> Unit,
): Job {
    //Pass initial value.
    callback(property.get(this))
    return properties.changes.onEach { name ->
        if (name.startsWith(property.name.asName())) {
            callback(property.get(this@useProperty))
        }
    }.launchIn(scope)
}