package space.kscience.visionforge

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.Value
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.parseAsName
import space.kscience.dataforge.names.startsWith
import kotlin.reflect.KProperty1

private fun Vision.withAncestors(): List<Vision> = buildList {
    add(this@withAncestors)
    var parent = parent
    while (parent != null) {
        add(parent)
        parent = parent.parent
    }
}

public fun Vision.inheritedEventFlow(): Flow<VisionEvent> =
    parent?.let { withAncestors().map { it.eventFlow }.merge() } ?: eventFlow


/**
 * Create a flow of a specific property
 */
public fun Vision.flowProperty(
    propertyName: Name,
    inherited: Boolean = isInheritedProperty(propertyName),
    useStyles: Boolean = isStyledProperty(propertyName),
): Flow<Meta?> = flow {
    //Pass initial value.
    emit(readProperty(propertyName, inherited, useStyles))

    val combinedFlow: Flow<VisionEvent> = if (inherited) {
        inheritedEventFlow()
    } else {
        eventFlow
    }

    combinedFlow.filterIsInstance<VisionPropertyChangedEvent>().collect { event ->
        if (event.propertyName.startsWith(propertyName) || (useStyles && event.propertyName == Vision.STYLE_KEY)) {
            emit(readProperty(event.propertyName, inherited, useStyles))
        }
    }
}

public fun Vision.flowProperty(
    propertyName: String,
    inherited: Boolean = isInheritedProperty(propertyName),
    useStyles: Boolean = isStyledProperty(propertyName),
): Flow<Meta?> = flowProperty(propertyName.parseAsName(), inherited, useStyles)

/**
 * Flow the value of specific property
 */
public fun Vision.flowPropertyValue(
    propertyName: Name,
    inherited: Boolean = isInheritedProperty(propertyName),
    useStyles: Boolean = isStyledProperty(propertyName),
): Flow<Value?> = flowProperty(propertyName, inherited, useStyles).map { it?.value }

public fun Vision.flowPropertyValue(
    propertyName: String,
    inherited: Boolean = isInheritedProperty(propertyName),
    useStyles: Boolean = isStyledProperty(propertyName),
): Flow<Value?> = flowPropertyValue(propertyName.parseAsName(), inherited, useStyles)

///

/**
 * Call [callback] on initial value of the property and then on all following values after change
 */
public fun Vision.useProperty(
    propertyName: Name,
    inherited: Boolean = isInheritedProperty(propertyName),
    useStyles: Boolean = isStyledProperty(propertyName),
    scope: CoroutineScope = manager?.context ?: error("Orphan Vision can't observe properties. Use explicit scope."),
    callback: suspend (Meta?) -> Unit,
): Job = scope.launch {
    //Pass initial value synchronously
    callback(readProperty(propertyName, inherited, useStyles))

    if (inherited) {
        inheritedEventFlow()
    } else {
        eventFlow
    }.filterIsInstance<VisionPropertyChangedEvent>().onEach { event ->
        if (event.propertyName.startsWith(propertyName) || (useStyles && event.propertyName == Vision.STYLE_KEY)) {
            callback(readProperty(event.propertyName, inherited, useStyles))
        }
    }.collect()
}

public fun Vision.useProperty(
    propertyName: String,
    inherited: Boolean = descriptor?.get(propertyName)?.inherited == true,
    useStyles: Boolean = descriptor?.get(propertyName)?.usesStyles != false,
    scope: CoroutineScope = manager?.context ?: error("Orphan Vision can't observe properties. Use explicit scope."),
    callback: suspend (Meta?) -> Unit,
): Job = useProperty(propertyName.parseAsName(), inherited, useStyles, scope, callback)

public fun <V : Vision, T> V.useProperty(
    property: KProperty1<V, T>,
    scope: CoroutineScope = manager?.context ?: error("Orphan Vision can't observe properties. Use explicit scope."),
    callback: suspend V.(T) -> Unit,
): Job = useProperty(property.name, scope = scope) {
    callback(property.get(this))
}

/**
 * Subscribe on property updates. The subscription is bound to the given scope and canceled when the scope is canceled
 */
public fun Vision.onPropertyChange(
    scope: CoroutineScope = manager?.context ?: error("Orphan Vision can't observe properties. Use explicit scope."),
    inherited: Boolean = true,
    callback: suspend (name: Name, value: Meta?) -> Unit,
): Job = if (inherited) {
    inheritedEventFlow()
} else {
    eventFlow
}.filterIsInstance<VisionPropertyChangedEvent>().onEach {
    callback(it.propertyName, it.propertyValue)
}.launchIn(scope)

/**
 * Observe changes to the specific property without passing the initial value.
 */
public fun <V : Vision, T> V.onPropertyChange(
    property: KProperty1<V, T>,
    scope: CoroutineScope = manager?.context ?: error("Orphan Vision can't observe properties. Use explicit scope."),
    callback: suspend V.(T) -> Unit,
): Job = inheritedEventFlow().filterIsInstance<VisionPropertyChangedEvent>().onEach {
    if (it.propertyName.startsWith(property.name)) {
        callback(property.get(this))
    }
}.launchIn(scope)