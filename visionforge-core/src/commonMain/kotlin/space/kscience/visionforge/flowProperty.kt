package space.kscience.visionforge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.Value
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.parseAsName
import space.kscience.dataforge.names.startsWith

/**
 * Create a flow of a specific property
 */
public fun Vision.flowProperty(
    propertyName: Name,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
): Flow<Meta> = flow {
    //Pass initial value.
    emit(properties.getMeta(propertyName, inherit, includeStyles))
    properties.changes.collect { name ->
        if (name.startsWith(propertyName)) {
            emit(properties.getMeta(propertyName, inherit, includeStyles))
        }
    }
}

public fun Vision.flowProperty(
    propertyName: String,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
): Flow<Meta> = flowProperty(propertyName.parseAsName(), inherit, includeStyles)

/**
 * Flow the value of specific property
 */
public fun Vision.flowPropertyValue(
    propertyName: Name,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
): Flow<Value?> = flow {
    //Pass initial value.
    emit(properties.getValue(propertyName, inherit, includeStyles))
    properties.changes.collect { name ->
        if (name.startsWith(propertyName)) {
            emit(properties.getValue(propertyName, inherit, includeStyles))
        }
    }
}

public fun Vision.flowPropertyValue(
    propertyName: String,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
): Flow<Value?> = flowPropertyValue(propertyName.parseAsName(), inherit, includeStyles)
