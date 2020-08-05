package hep.dataforge.properties

import hep.dataforge.meta.DFExperimental
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

//TODO move to core

@DFExperimental
interface Property<T> {
    var value: T

    fun onChange(owner: Any? = null, callback: (T) -> Unit)
    fun removeChangeListener(owner: Any? = null)
}

@DFExperimental
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Property<T>.flow() = callbackFlow<T> {
    send(value)
    onChange(this) {
        //TODO add exception handler?
        launch {
            send(it)
        }
    }
    awaitClose { removeChangeListener(this) }
}

/**
 * Reflect all changes in the [source] property onto this property
 *
 * @return a mirroring job
 */
@DFExperimental
fun <T> Property<T>.mirror(source: Property<T>, scope: CoroutineScope): Job {
    return scope.launch {
        source.flow().collect {
            value = it
        }
    }
}

/**
 * Bi-directional connection between properties
 */
@DFExperimental
fun <T> Property<T>.bind(other: Property<T>) {
    onChange(other) {
        other.value = it
    }
    other.onChange {
        this.value = it
    }
}