package hep.dataforge.properties

import hep.dataforge.meta.Config
import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.get
import hep.dataforge.names.Name

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