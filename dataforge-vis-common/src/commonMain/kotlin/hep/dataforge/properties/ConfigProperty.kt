package hep.dataforge.properties

import hep.dataforge.meta.*
import hep.dataforge.names.Name

@DFExperimental
class ConfigProperty(val config: Config, val name: Name) : Property<MetaItem<*>?> {
    override var value: MetaItem<*>?
        get() = config[name]
        set(value) {
            config[name] = value
        }

    override fun onChange(owner: Any?, callback: (MetaItem<*>?) -> Unit) {
        config.onChange(owner) { name, oldItem, newItem ->
            if (name == this.name && oldItem != newItem) callback(newItem)
        }
    }

    override fun removeChangeListener(owner: Any?) {
        config.removeListener(owner)
    }
}