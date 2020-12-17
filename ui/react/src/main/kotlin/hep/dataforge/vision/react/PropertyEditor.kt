package hep.dataforge.vision.react

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.*
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.lastOrNull
import hep.dataforge.names.plus
import hep.dataforge.values.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import react.*
import react.dom.render
import styled.*

public external interface PropertyEditorItemProps : RProps {

    /**
     * Root config object - always non null
     */
    public var provider: MutableItemProvider

    /**
     * Full path to the displayed node in [provider]. Could be empty
     */
    public var name: Name

    /**
     * Root descriptor
     */
    public var descriptor: NodeDescriptor?


    public var scope: CoroutineScope?

    /**
     *
     */
    public var updateFlow: Flow<Name>?
}

private val PropertyEditorItem: FunctionalComponent<PropertyEditorItemProps> =
    functionalComponent("ConfigEditorItem") { props ->
        propertyEditorItem(props)
    }

private fun RBuilder.propertyEditorItem(props: PropertyEditorItemProps) {
    var expanded: Boolean by useState { true }
    var item: MetaItem<*>? by useState { props.provider.getItem(props.name) }
    val descriptorItem: ItemDescriptor? = props.descriptor?.get(props.name)
    var actualItem: MetaItem<Meta>? by useState { item ?: descriptorItem?.defaultItem() }

    val token = props.name.lastOrNull()?.toString() ?: "Properties"

    fun update() {
        item = props.provider.getItem(props.name)
        actualItem = item ?: descriptorItem?.defaultItem()
    }

    if (props.updateFlow != null) {
        useEffectWithCleanup(listOf(props.provider, props.updateFlow)) {
            val updateJob = props.updateFlow!!.onEach { updatedName ->
                if (updatedName == props.name) {
                    update()
                }
            }.launchIn(props.scope ?: GlobalScope)
            return@useEffectWithCleanup { updateJob.cancel() }
        }
    }

    val expanderClick: (Event) -> Unit = {
        expanded = !expanded
    }

    val valueChanged: (Value?) -> Unit = {
        if (it == null) {
            props.provider.remove(props.name)
        } else {
            props.provider[props.name] = it
        }
        update()
    }

    val removeClick: (Event) -> Unit = {
        props.provider.remove(props.name)
        update()
    }



    if (actualItem is MetaItem.NodeItem) {
        styledDiv {
            css {
                +TreeStyles.treeLeaf
            }
            styledSpan {
                css {
                    +TreeStyles.treeCaret
                    if (expanded) {
                        +TreeStyles.treeCaredDown
                    }
                }
                attrs {
                    onClickFunction = expanderClick
                }
            }
            styledSpan {
                css {
                    +TreeStyles.treeLabel
                    if (item == null) {
                        +TreeStyles.treeLabelInactive
                    }
                }
                +token
            }
        }
        if (expanded) {
            styledUl {
                css {
                    +TreeStyles.tree
                }
                val keys = buildSet {
                    (descriptorItem as? NodeDescriptor)?.items?.keys?.forEach {
                        add(NameToken(it))
                    }
                    item?.node?.items?.keys?.let { addAll(it) }
                }

                keys.filter { !it.body.startsWith("@") }.forEach { token ->
                    styledLi {
                        css {
                            +TreeStyles.treeItem
                        }
                        child(PropertyEditorItem) {
                            attrs {
                                this.key = props.name.toString()
                                this.provider = props.provider
                                this.name = props.name + token
                                this.descriptor = props.descriptor
                            }
                        }
                        //configEditor(props.root, props.name + token, props.descriptor, props.default)
                    }
                }
            }
        }
    } else {
        styledDiv {
            css {
                +TreeStyles.treeLeaf
            }
            styledDiv {
                css {
                    +TreeStyles.treeLabel
                }
                styledSpan {
                    css {
                        if (item == null) {
                            +TreeStyles.treeLabelInactive
                        }
                    }
                    +token
                }
            }
            styledDiv {
                css {
                    +TreeStyles.resizeableInput
                }
                valueChooser(
                    props.name,
                    actualItem,
                    descriptorItem as? ValueDescriptor,
                    valueChanged
                )
            }
            styledButton {
                css {
                    +TreeStyles.removeButton
                }
                +"\u00D7"
                attrs {
                    if (item == null) {
                        disabled = true
                    } else {
                        onClickFunction = removeClick
                    }
                }
            }

        }
    }
}

public external interface PropertyEditorProps : RProps {
    public var provider: MutableItemProvider
    public var updateFlow: Flow<Name>?
    public var descriptor: NodeDescriptor?
    public var scope: CoroutineScope?
}

@JsExport
public val PropertyEditor: FunctionalComponent<PropertyEditorProps> = functionalComponent("ConfigEditor") { props ->
    child(PropertyEditorItem) {
        attrs {
            this.key = ""
            this.provider = props.provider
            this.name = Name.EMPTY
            this.descriptor = props.descriptor
            this.scope = props.scope
        }
    }
}

public fun RBuilder.propertyEditor(
    provider: MutableItemProvider,
    updateFlow: Flow<Name>? = null,
    descriptor: NodeDescriptor? = null,
    key: Any? = null,
    scope: CoroutineScope? = null,
) {
    child(PropertyEditor) {
        attrs {
            this.key = key?.toString() ?: ""
            this.provider = provider
            this.updateFlow = updateFlow
            this.descriptor = descriptor
            this.scope = scope
        }
    }
}

private fun Config.flowUpdates(): Flow<Name> = callbackFlow {
    onChange(this) { name, _, _ ->
        launch {
            send(name)
        }
    }
    awaitClose {
        removeListener(this)
    }
}

public fun MutableItemProvider.withDefault(default: ItemProvider): MutableItemProvider = object : MutableItemProvider {
    override fun getItem(name: Name): MetaItem<*>? = getItem(name) ?: default.getItem(name)

    override fun setItem(name: Name, item: MetaItem<*>?) = this@withDefault.setItem(name, item)
}



public fun RBuilder.configEditor(
    config: Config,
    descriptor: NodeDescriptor? = null,
    default: Meta? = null,
    key: Any? = null,
    scope: CoroutineScope? = null,
) = propertyEditor(config.withDefault(default ?: ItemProvider.EMPTY), config.flowUpdates(), descriptor, key, scope)

public fun Element.configEditor(
    config: Config,
    descriptor: NodeDescriptor? = null,
    default: Meta? = null,
    key: Any? = null,
    scope: CoroutineScope? = null,
) {
    render(this) {
        configEditor(config,descriptor,default, key, scope)
    }
}