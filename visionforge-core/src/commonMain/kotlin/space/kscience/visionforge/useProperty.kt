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
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.names.startsWith
import kotlin.reflect.KProperty1

/**
 * Returns this vision together with its ancestor chain.
 *
 * The returned list is ordered from this vision to the root vision, following
 * each non-null parent reference until no parent is available.
 *
 * @return a list containing this vision followed by all of its ancestors
 */
private fun Vision.withAncestors(): List<Vision> = buildList {
    add(this@withAncestors)
    var parent = parent
    while (parent != null) {
        add(parent)
        parent = parent.parent
    }
}

/**
 * Returns a flow of events emitted by this vision and, when available, its ancestor chain.
 *
 * If this vision has no parent, the returned flow is this vision's own event flow.
 * Otherwise, the event flows of this vision and all ancestors up to the root are merged.
 *
 * @return a flow emitting [VisionEvent] values from this vision and its ancestors
 */
public fun Vision.inheritedEventFlow(): Flow<VisionEvent> =
    parent?.let { withAncestors().map { it.eventFlow }.merge() } ?: eventFlow


private fun Vision.isInvalidationEvent(propertyName: Name, event: VisionPropertyChangedEvent): Boolean {
    if (event.propertyName.startsWith(propertyName)) return true

    if (event.propertyName == Vision.STYLE_KEY) return true

    styles.forEach {
        if (event.propertyName.startsWith(Vision.STYLESHEET_KEY + it)) return true
    }

    return false
}

private fun Vision.flowPropertyChangeEvents(
    propertyName: Name,
    inherited: Boolean = isInheritedProperty(propertyName),
    useStyles: Boolean = isStyledProperty(propertyName),
): Flow<VisionPropertyChangedEvent> = if (inherited || useStyles) {
    inheritedEventFlow()
} else {
    eventFlow
}.filterIsInstance<VisionPropertyChangedEvent>().filter {
    isInvalidationEvent(propertyName, it)
}

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

    emitAll(
        flowPropertyChangeEvents(propertyName, inherited, useStyles).map {
            readProperty(
                name = propertyName,
                inherited = inherited,
                useStyles = useStyles
            )
        }
    )
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

/**
 * Flow the value of specific property
 */
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
): Job = flowProperty(propertyName, inherited, useStyles).onEach {
    callback(it)
}.launchIn(scope)

public fun Vision.useProperty(
    propertyName: String,
    inherited: Boolean = descriptor?.get(propertyName)?.inherited == true,
    useStyles: Boolean = descriptor?.get(propertyName)?.usesStyles != false,
    scope: CoroutineScope = manager?.context ?: error("Orphan Vision can't observe properties. Use explicit scope."),
    callback: suspend (Meta?) -> Unit,
): Job = useProperty(propertyName.parseAsName(), inherited, useStyles, scope, callback)

/**
 * Starts observing the property represented by [property].
 *
 * Whenever the observed property is updated, [callback] is invoked with the value read from the
 * referenced Kotlin property on this [Vision].
 *
 * @param property Kotlin property reference whose name is used to subscribe to property changes.
 * @param scope Coroutine scope used to run the observation. Defaults to the owning manager context.
 * @param callback Suspending callback invoked on this [Vision] with the current property value.
 * @return A [Job] representing the active observation. Cancel it to stop observing the property.
 */
public fun <V : Vision, T> V.useProperty(
    property: KProperty1<V, T>,
    scope: CoroutineScope = manager?.context ?: error("Orphan Vision can't observe properties. Use explicit scope."),
    callback: suspend V.(T) -> Unit,
): Job = scope.launch {
    callback(property.get(this@useProperty))

    flowPropertyChangeEvents(Name.of(property.name)).collect {
        callback(property.get(this@useProperty))
    }
}

/**
 * Observe changes to the specific property without passing the initial value.
 */
public fun <V : Vision, T> V.onPropertyChange(
    property: KProperty1<V, T>,
    scope: CoroutineScope = manager?.context ?: error("Orphan Vision can't observe properties. Use explicit scope."),
    callback: suspend V.(T) -> Unit,
): Job = flowPropertyChangeEvents(Name.of(property.name)).onEach {
    callback(property.get(this))
}.launchIn(scope)

/**
 * Subscribe on property updates. The subscription is bound to the given scope and canceled when the scope is canceled
 */
public fun Vision.onPropertyChange(
    scope: CoroutineScope = manager?.context ?: error("Orphan Vision can't observe properties. Use explicit scope."),
    inherited: Boolean = true,
    callback: suspend (name: Name, value: Meta?) -> Unit,
): Job {
    val combinedFlow: Flow<VisionEvent> = if (inherited) {
        inheritedEventFlow()
    } else {
        eventFlow
    }
    return combinedFlow.filterIsInstance<VisionPropertyChangedEvent>().onEach {
        callback(it.propertyName, it.propertyValue)
    }.launchIn(scope)
}