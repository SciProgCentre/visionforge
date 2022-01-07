package space.kscience.visionforge

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.dataforge.names.*
import kotlin.jvm.Synchronized

private class StructureChangeListener(val owner: Any?, val callback: VisionGroup.(Name) -> Unit)

/**
 * Abstract implementation of mutable group of [Vision]
 *
 * @param childrenInternal Internal mutable container for group children
 */
@Serializable
@SerialName("vision.group")
public open class VisionGroupBase(
    @EncodeDefault @SerialName("children") protected val childrenInternal: MutableMap<NameToken, Vision> = LinkedHashMap(),
) : VisionBase(), MutableVisionGroup {

    /**
     * A map of top level named children
     */
    override val children: Map<NameToken, Vision> get() = childrenInternal

    init {
        childrenInternal.forEach { (token, child) ->
            if (child.parent != null && child.parent != this) error("Can't reassign existing parent for child $token")
            child.parent = this
        }
    }

    override fun invalidateProperty(propertyName: Name) {
        super.invalidateProperty(propertyName)
        for (obj in this) {
            obj.invalidateProperty(propertyName)
        }
    }

    @Transient
    private val structureListeners = HashSet<StructureChangeListener>()

    @Synchronized
    override fun onStructureChanged(owner: Any?, block: VisionGroup.(Name) -> Unit) {
        structureListeners.add(StructureChangeListener(owner, block))
    }

    @Synchronized
    override fun removeStructureListener(owner: Any?) {
        structureListeners.removeAll { it.owner == owner }
    }

    /**
     * Propagate children change event upwards
     */
    protected fun childrenChanged(name: Name) {
        structureListeners.forEach {
            it.callback(this, name)
        }
    }

    /**
     * Add a static child. Statics could not be found by name, removed or replaced. Changing statics also do not trigger events.
     */
    protected open fun addStatic(child: Vision): Unit {
        attachChild(NameToken("@static", index = child.hashCode().toString()), child)
    }

    /**
     * Create a vision group of the same type as this vision group. Do not attach it.
     */
    protected open fun createGroup(): VisionGroupBase = VisionGroupBase()

    /**
     * Set parent for given child and attach it
     */
    private fun attachChild(token: NameToken, child: Vision?) {
        val before = childrenInternal[token]
        when {
            child == null -> {
                childrenInternal.remove(token)
            }
            child.parent == null -> {
                child.parent = this
                childrenInternal[token] = child
            }
            child.parent !== this -> {
                error("Can't reassign existing parent for child $token")
            }
        }
        if (before != child) {
            childrenChanged(token.asName())
            if (child is MutableVisionGroup) {
                child.onStructureChanged(this) { changedName ->
                    this@VisionGroupBase.childrenChanged(token + changedName)
                }
            }
        }
    }

    /**
     * Recursively create a child group
     */
    private fun createGroups(name: Name): VisionGroupBase = when {
        name.isEmpty() -> error("Should be unreachable")
        name.length == 1 -> {
            val token = name.tokens.first()
            when (val current = children[token]) {
                null -> createGroup().also { child ->
                    attachChild(token, child)
                }
                is VisionGroupBase -> current
                else -> error("Can't create group with name $name because it exists and not a group")
            }
        }
        else -> createGroups(name.tokens.first().asName()).createGroups(name.cutFirst())
    }

    /**
     * Add named or unnamed child to the group. If key is null the child is considered unnamed. Both key and value are not
     * allowed to be null in the same time. If name is present and [child] is null, the appropriate element is removed.
     */
    override fun set(name: Name?, child: Vision?): Unit {
        when {
            name == null -> {
                if (child != null) {
                    addStatic(child)
                }
            }
            name.isEmpty() -> error("Empty names are not allowed in VisionGroup::set")
            name.length == 1 -> {
                val token = name.tokens.first()
                attachChild(token, child)
            }
            else -> {
                //TODO add safety check
                val parent = (get(name.cutLast()) as? MutableVisionGroup) ?: createGroups(name.cutLast())
                parent[name.tokens.last().asName()] = child
            }
        }
    }

    override fun update(change: VisionChange) {
        change.children?.forEach { (name, change) ->
            when {
                change.delete -> set(name, null)
                change.vision != null -> set(name, change.vision)
                else -> get(name)?.update(change)
            }
        }
        super.update(change)
    }
}

/**
 * Non-serializable root group used to propagate manager to its children
 */
internal class RootVisionGroup(override val manager: VisionManager) : VisionGroupBase()

/**
 * Designate this [VisionGroup] as a root and assign a [VisionManager] as its parent
 */
public fun Vision.setAsRoot(manager: VisionManager) {
    if (parent != null) error("Vision $this already has a parent. It could not be set as root")
    parent = RootVisionGroup(manager)
}