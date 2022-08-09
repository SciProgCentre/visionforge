package space.kscience.visionforge

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor


@Serializable
public abstract class AbstractVision : Vision {

    @Transient
    override var parent: Vision? = null

    @SerialName("properties")
    protected var propertiesInternal: MutableMeta? = null

    final override val properties: MutableVisionProperties by lazy {
        object : AbstractVisionProperties(this) {
            override var properties: MutableMeta?
                get() = propertiesInternal
                set(value) {
                    propertiesInternal = value
                }
        }
    }

    override val descriptor: MetaDescriptor? get() = null
}