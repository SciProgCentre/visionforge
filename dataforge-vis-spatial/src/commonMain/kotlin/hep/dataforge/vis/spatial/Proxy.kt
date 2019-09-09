@file:UseSerializers(Point3DSerializer::class, NameSerializer::class, ConfigSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.io.ConfigSerializer
import hep.dataforge.io.NameSerializer
import hep.dataforge.meta.Config
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.MetaItem
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.asName
import hep.dataforge.vis.common.AbstractVisualObject
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers

/**
 * A proxy [VisualObject3D] to reuse a template object
 */
@Serializable
class Proxy(val templateName: Name) : AbstractVisualObject(), VisualGroup, VisualObject3D {

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

    /**
     * Recursively search for defined template in the parent
     */
    val template: VisualObject3D by lazy {
        (parent as? VisualGroup3D)?.getTemplate(templateName)
            ?: error("Template with name $templateName not found in $parent")
    }

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            super.getProperty(name, false) ?: template.getProperty(name, false) ?: parent?.getProperty(name, inherit)
        } else {
            super.getProperty(name, false) ?: template.getProperty(name, false)
        }
    }

    override fun MetaBuilder.updateMeta() {
        //TODO add reference to child
        updatePosition()
    }

    override val children: Map<NameToken, ProxyChild>
        get() = (template as? VisualGroup)?.children?.mapValues {
            ProxyChild(it.key.asName())
        } ?: emptyMap()

    private data class ProxyChangeListeners(val owner: Any?, val callback: (Name, VisualObject?) -> Unit)

    @Transient
    private val listeners = HashSet<ProxyChangeListeners>()

    override fun onChildrenChange(owner: Any?, action: (Name, VisualObject?) -> Unit) {
        listeners.add(ProxyChangeListeners(owner, action))
    }

    override fun removeChildrenChangeListener(owner: Any?) {
        listeners.removeAll { it.owner == owner }
    }

    override fun set(name: Name, child: VisualObject?) {
        error("Content change not supported for proxy")
    }

    private val propertyCache: HashMap<Name, Config> = HashMap()

    private fun Config.attachListener(obj: VisualObject) {
        onChange(this@Proxy) { name, before, after ->
            listeners.forEach { listener ->
                listener.callback(name, obj)
            }
        }
    }

    inner class ProxyChild(val name: Name) : AbstractVisualObject() {
        override var properties: Config?
            get() = propertyCache.getOrPut(name) {
                Config().apply {
                    attachListener(this@ProxyChild)
                }
            }
            set(value) {
                if (value == null) {
                    propertyCache.remove(name)?.removeListener(this@Proxy)
                } else {
                    propertyCache[name] = value.apply {
                        attachListener(this@ProxyChild)
                    }
                }
            }

    }
}

inline fun VisualGroup3D.ref(
    templateName: Name,
    name: String = "",
    action: Proxy.() -> Unit = {}
) = Proxy(templateName).apply(action).also { set(name, it) }