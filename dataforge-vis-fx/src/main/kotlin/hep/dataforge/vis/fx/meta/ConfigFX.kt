//package hep.dataforge.vis.fx.meta
//
//import hep.dataforge.descriptors.NodeDescriptor
//import hep.dataforge.descriptors.ValueDescriptor
//import hep.dataforge.meta.Config
//import hep.dataforge.meta.Meta
//import hep.dataforge.names.Name
//import hep.dataforge.values.Null
//import hep.dataforge.values.Value
//import javafx.beans.binding.StringBinding
//import javafx.beans.property.SimpleObjectProperty
//import javafx.beans.property.SimpleStringProperty
//import javafx.beans.value.ObservableBooleanValue
//import javafx.beans.value.ObservableStringValue
//import javafx.collections.FXCollections
//import javafx.collections.ObservableList
//import javafx.scene.control.TreeItem
//import tornadofx.*
//
//class ConfigTreeItem(configFX: ConfigFX) : TreeItem<ConfigFX>(configFX) {
//    init {
//        this.children.bind(value.children) { ConfigTreeItem(it) }
//    }
//
//    override fun isLeaf(): Boolean = value is ConfigFXValue
//}
//
//
///**
// * A node, containing relative representation of configuration node and description
// * Created by darksnake on 01-May-17.
// */
//sealed class ConfigFX(name: String) {
//
//    val nameProperty = SimpleStringProperty(name)
//    val name by nameProperty
//
//    val parentProperty = SimpleObjectProperty<ConfigFXNode>()
//    val parent by parentProperty
//
//    abstract val hasValueProperty: ObservableBooleanValue
//    //abstract val hasDefaultProperty: ObservableBooleanValue
//
//    abstract val descriptionProperty: ObservableStringValue
//
//    abstract val children: ObservableList<ConfigFX>
//
//    /**
//     * remove itself from parent
//     */
//    abstract fun remove()
//
//    abstract fun invalidate()
//}
//
//
///**
// * Tree item for node
// * Created by darksnake on 30-Apr-17.
// */
//open class ConfigFXNode(name: String, parent: ConfigFXNode? = null) : ConfigFX(name) {
//
//    final override val hasValueProperty = parentProperty.booleanBinding(nameProperty) {
//        it?.config?.hasMeta(this.name) ?: false
//    }
//
//    /**
//     * A descriptor that could be manually set to the node
//     */
//    val descriptorProperty = SimpleObjectProperty<NodeDescriptor?>()
//
//    /**
//     * Actual descriptor which holds value inferred from parrent
//     */
//    private val actualDescriptor = objectBinding(descriptorProperty, parentProperty, nameProperty) {
//        value ?: parent?.descriptor?.getNodeDescriptor(name)
//    }
//
//    val descriptor: NodeDescriptor? by actualDescriptor
//
//    val configProperty = SimpleObjectProperty<Config?>()
//
//    private val actualConfig = objectBinding(configProperty, parentProperty, nameProperty) {
//        value ?: parent?.config?.getMetaList(name)?.firstOrNull()
//    }
//
//    val config: Config? by actualConfig
//
//    final override val descriptionProperty: ObservableStringValue = stringBinding(actualDescriptor) {
//        value?.info ?: ""
//    }
//
//    override val children: ObservableList<ConfigFX> = FXCollections.observableArrayList<ConfigFX>()
//
//    init {
//        parentProperty.set(parent)
//        hasValueProperty.onChange {
//            parent?.hasValueProperty?.invalidate()
//        }
//        invalidate()
//    }
//
//    /**
//     * Get existing configuration node or create and attach new one
//     *
//     * @return
//     */
//    private fun getOrBuildNode(): Config {
//        return config ?: if (parent == null) {
//            throw RuntimeException("The configuration for root node is note defined")
//        } else {
//            parent.getOrBuildNode().requestNode(name)
//        }
//    }
//
//    fun addValue(name: String) {
//        getOrBuildNode().setValue(name, Null)
//    }
//
//    fun setValue(name: String, value: Value) {
//        getOrBuildNode().setValue(name, value)
//    }
//
//    fun removeValue(valueName: String) {
//        config?.removeValue(valueName)
//        children.removeIf { it.name == name }
//    }
//
//    fun addNode(name: String) {
//        getOrBuildNode().requestNode(name)
//    }
//
//    fun removeNode(name: String) {
//        config?.removeNode(name)
//    }
//
//    override fun remove() {
//        //FIXME does not work on multinodes
//        parent?.removeNode(name)
//        invalidate()
//    }
//
//    final override fun invalidate() {
//        actualDescriptor.invalidate()
//        actualConfig.invalidate()
//        hasValueProperty.invalidate()
//
//        val nodeNames = ArrayList<String>()
//        val valueNames = ArrayList<String>()
//
//        config?.apply {
//            nodeNames.addAll(this.nodeNames.toList())
//            valueNames.addAll(this.valueNames.toList())
//        }
//
//        descriptor?.apply {
//            nodeNames.addAll(childrenDescriptors().keys)
//            valueNames.addAll(valueDescriptors().keys)
//        }
//
//        //removing old values
//        children.removeIf { !(valueNames.contains(it.name) || nodeNames.contains(it.name)) }
//
//        valueNames.forEach { name ->
//            children.find { it.name == name }?.invalidate().orElse {
//                children.add(ConfigFXValue(name, this))
//            }
//        }
//
//        nodeNames.forEach { name ->
//            children.find { it.name == name }?.invalidate().orElse {
//                children.add(ConfigFXNode(name, this))
//            }
//        }
//        children.sortBy { it.name }
//    }
//
//    fun updateValue(path: Name, value: Value?) {
//        when {
//            path.length == 0 -> kotlin.error("Path never could be empty when updating value")
//            path.length == 1 -> {
//                val hasDescriptor = descriptor?.getValueDescriptor(path) != null
//                if (value == null && !hasDescriptor) {
//                    //removing the value if it is present
//                    children.removeIf { it.name == path.unescaped }
//                } else {
//                    //invalidating value if it is present
//                    children.find { it is ConfigFXValue && it.name == path.unescaped }?.invalidate().orElse {
//                        //adding new node otherwise
//                        children.add(ConfigFXValue(path.unescaped, this))
//                    }
//                }
//            }
//            path.length > 1 -> children.filterIsInstance<ConfigFXNode>().find { it.name == path.first.unescaped }?.updateValue(
//                path.cutFirst(),
//                value
//            )
//        }
//    }
//
//    fun updateNode(path: Name, list: List<Meta>) {
//        when {
//            path.isEmpty() -> invalidate()
//            path.length == 1 -> {
//                val hasDescriptor = descriptor?.getNodeDescriptor(path.unescaped) != null
//                if (list.isEmpty() && !hasDescriptor) {
//                    children.removeIf { it.name == path.unescaped }
//                } else {
//                    children.find { it is ConfigFXNode && it.name == path.unescaped }?.invalidate().orElse {
//                        children.add(ConfigFXNode(path.unescaped, this))
//                    }
//                }
//            }
//            else -> children.filterIsInstance<ConfigFXNode>().find { it.name == path.first.toString() }?.updateNode(
//                path.cutFirst(),
//                list
//            )
//        }
//    }
//}
//
//class ConfigFXRoot(rootConfig: Config, rootDescriptor: NodeDescriptor? = null) : ConfigFXNode(rootConfig.name),
//    ConfigChangeListener {
//
//    init {
//        configProperty.set(rootConfig)
//        descriptorProperty.set(rootDescriptor)
//        rootConfig.addListener(this)
//        invalidate()
//    }
//
//    override fun notifyValueChanged(name: Name, oldItem: Value?, newItem: Value?) {
//        updateValue(name, newItem)
//    }
//
//    override fun notifyNodeChanged(nodeName: Name, oldItem: List<Meta>, newItem: List<Meta>) {
//        updateNode(nodeName, newItem)
//    }
//}
//
//
///**
// * Created by darksnake on 01-May-17.
// */
//class ConfigFXValue(name: String, parent: ConfigFXNode) : ConfigFX(name) {
//
//    init {
//        parentProperty.set(parent)
//    }
//
//    override val hasValueProperty = parentProperty.booleanBinding(nameProperty) {
//        it?.config?.hasValue(this.name) ?: false
//    }
//
//
//    override val children: ObservableList<ConfigFX> = FXCollections.emptyObservableList()
//
//    val descriptor: ValueDescriptor? = parent.descriptor?.values[name]
//
//    override val descriptionProperty: ObservableStringValue = object : StringBinding() {
//        override fun computeValue(): String {
//            return descriptor?.info ?: ""
//        }
//    }
//
//    val valueProperty = parentProperty.objectBinding(nameProperty) {
//        parent.config?.optValue(name).nullable ?: descriptor?.default
//    }
//
//    var value: Value
//        set(value) {
//            parent?.setValue(name, value)
//        }
//        get() = valueProperty.value ?: Value.NULL
//
//
//    override fun remove() {
//        parent?.removeValue(name)
//        invalidate()
//    }
//
//    override fun invalidate() {
//        valueProperty.invalidate()
//        hasValueProperty.invalidate()
//    }
//}
