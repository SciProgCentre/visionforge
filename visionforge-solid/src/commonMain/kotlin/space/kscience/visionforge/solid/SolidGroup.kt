package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.*
import space.kscience.visionforge.*


/**
 * A container with prototype support
 */
public interface PrototypeHolder {
    /**
     * Build or update the prototype tree
     */
    public fun prototypes(builder: SolidGroup.() -> Unit)

    /**
     * Resolve a prototype from this container. Should never return a ref.
     */
    public fun getPrototype(name: Name): Solid?
}

public fun PrototypeHolder.getPrototype(name: String): Solid? = getPrototype(Name.parse(name))

public interface SolidContainer : VisionGroup<Solid>, Solid {
    override suspend fun receiveEvent(event: VisionEvent) {
        super<VisionGroup>.receiveEvent(event)
    }
}

/**
 * A [Solid] group with additional accessor methods
 */
@Serializable
@SerialName("group.solid")
@VisionBuilder
public class SolidGroup : AbstractVision(), SolidContainer, PrototypeHolder, MutableVisionGroup<Solid> {

    private val solids = LinkedHashMap<NameToken, Solid>()

    override val visions: Map<NameToken, Solid> get() = solids

    private var prototypes: SolidGroup? = null


    // ensure proper children links after deserialization
    init {
        solids.forEach { it.value.parent = this }
        prototypes?.parent = this
    }


    override val descriptor: MetaDescriptor get() = Solid.descriptor

    override fun convertVisionOrNull(vision: Vision): Solid? = vision as? Solid

    override suspend fun receiveEvent(event: VisionEvent) {
        super<MutableVisionGroup>.receiveEvent(event)
    }

    /**
     * Get a prototype redirecting the request to the parent if prototype is not found.
     * If a prototype is a ref, then it is unfolded automatically.
     */
    override fun getPrototype(name: Name): Solid? =
        prototypes?.getVision(name) ?: (parent as? PrototypeHolder)?.getPrototype(name)

    /**
     * Create or edit prototype node as a group
     */
    override fun prototypes(builder: SolidGroup.() -> Unit): Unit {
        (prototypes ?: SolidGroup().also {
            prototypes = it
            it.parent = this
        }).apply(builder)
    }

    override val defaultChainTarget: String get() = VisionGroup.VISION_CHILD_TARGET

    override fun setVision(token: NameToken, vision: Solid?) {
        if (vision == null) {
            solids.remove(token)?.also {
                it.parent = null
            }
        } else {
            solids[token] = vision
            vision.parent = this
        }
        emitEvent(VisionGroupCompositionChangedEvent(token, vision))
    }

    public companion object {
        public const val STATIC_TOKEN_BODY: String = "@static"

        public fun staticNameFor(vision: Vision): NameToken =
            NameToken(STATIC_TOKEN_BODY, vision.hashCode().toString(16))

        public fun inferNameFor(name: String?, vision: Vision): NameToken =
            name?.let(NameToken::parse) ?: staticNameFor(vision)
    }
}

public fun SolidContainer.getVision(name: Name): Solid? = when (name.length) {
    0 -> this
    1 -> getVision(name.first())
    else -> (getVision(name.first()) as? SolidContainer)?.getVision(name.cutFirst())
}

public fun SolidContainer.getVision(name: String): Solid? = getVision(name.parseAsName())

public operator fun SolidGroup.get(name: NameToken): Solid? = getVision(name)

public operator fun SolidGroup.get(name: Name): Solid? = getVision(name)

public operator fun SolidGroup.get(name: String): Solid? = getVision(name)


public operator fun SolidGroup.set(name: NameToken, value: Solid?): Unit = setVision(name, value)

public operator fun SolidGroup.set(name: Name, vision: Solid?) {
    when (name.length) {
        0 -> error("Can't set vision with empty path")
        1 -> {
            val token = name.first()
            setVision(token, vision)
        }

        else -> {
            val token = name.first()
            when (val existing = getVision(token)) {
                null -> SolidGroup().also { newGroup ->
                    setVision(token, newGroup)
                    newGroup[name.cutFirst()] = vision
                }

                is SolidGroup -> existing[name.cutFirst()] = vision

                else -> error("Can't set Solid by path because of existing non-group Solid at $token")
            }
        }
    }
}

public operator fun SolidGroup.set(name: String, vision: Solid?): Unit = set(name.parseAsName(), vision)

/**
 * Add anonymous (auto-assigned name) child to a SolidGroup
 */
public fun MutableVisionContainer<Solid>.static(solid: Solid) {
    setVision(SolidGroup.staticNameFor(solid), solid)
}

public inline fun MutableVisionContainer<Solid>.solidGroup(
    token: NameToken? = null,
    builder: SolidGroup.() -> Unit = {},
): SolidGroup = SolidGroup().also {
    setVision(token ?: SolidGroup.staticNameFor(it), it)
}.apply(builder)
//root first, update later

/**
 * Define a group with given [token], attach it to this parent and return it.
 */
public inline fun MutableVisionContainer<Solid>.solidGroup(
    token: String,
    action: SolidGroup.() -> Unit = {},
): SolidGroup = solidGroup(NameToken.parse(token), action)

/**
 * Create a [SolidGroup] using given configuration [block]
 */
public inline fun SolidGroup(block: SolidGroup.() -> Unit): SolidGroup = SolidGroup().apply(block)