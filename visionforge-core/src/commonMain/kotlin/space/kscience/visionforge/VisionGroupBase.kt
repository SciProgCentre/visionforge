package space.kscience.visionforge

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.dataforge.names.*


/**
 * Abstract implementation of mutable group of [Vision]
 *
 * @param childrenInternal Internal mutable container for group children
 */
@Serializable
@SerialName("vision.group")
public open class VisionGroupBase(
    @SerialName("children") internal val childrenInternal: MutableMap<NameToken, Vision> = LinkedHashMap(),
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
    private val _structureChanges: MutableSharedFlow<MutableVisionGroup.StructureChange> = MutableSharedFlow()

    override val structureChanges: SharedFlow<MutableVisionGroup.StructureChange> get() = _structureChanges

    /**
     * Propagate children change event upwards
     */
    private fun childrenChanged(name: NameToken, before: Vision?, after: Vision?) {
        launch {
            _structureChanges.emit(MutableVisionGroup.StructureChange(name, before, after))
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
        val before = children[token]
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
            childrenChanged(token, before, child)
        }
    }

    /**
     * Recursively create a child group
     */
    private fun createGroups(name: Name): VisionGroupBase {
        return when {
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
                change.void -> set(name, null)
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
 * Designate this [VisionGroup] as a root group and assign a [VisionManager] as its parent
 */
public fun Vision.root(manager: VisionManager){
    parent = RootVisionGroup(manager)
}