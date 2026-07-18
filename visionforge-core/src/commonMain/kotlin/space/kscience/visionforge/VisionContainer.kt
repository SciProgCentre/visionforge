package space.kscience.visionforge

import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@DslMarker
public annotation class VisionBuilder

/**
 * A container interface with read access to its content
 * using DataForge [Name] objects as keys.
 */
public interface VisionContainer<out V : Vision> {
    public fun getVision(token: NameToken): V?
}

public fun <V : Vision> VisionContainer<V>.getVision(token: String): V? = getVision(NameToken.parse(token))

/**
 * A container interface with write/replace/delete access to its content.
 */
@VisionBuilder
public interface MutableVisionContainer<in V : Vision> {
    //TODO add documentation
    public fun setVision(token: NameToken, vision: V?)

    public companion object {
        @OptIn(ExperimentalUuidApi::class)
        public fun generateID(): NameToken = NameToken("@vision", Uuid.random().toHexString())
    }
}