package space.kscience.visionforge

import kotlinx.serialization.*
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor


@Serializable
public abstract class AbstractVision : Vision {

    @Transient
    override var parent: Vision? = null

    @SerialName("properties")
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    protected var propertiesInternal: MutableMeta = MutableMeta()

    final override val properties: MutableVisionProperties by lazy {
        AbstractVisionProperties(this, propertiesInternal)
    }

    override val descriptor: MetaDescriptor? get() = null
}