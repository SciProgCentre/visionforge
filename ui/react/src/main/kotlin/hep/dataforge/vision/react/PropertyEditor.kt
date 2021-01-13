package hep.dataforge.vision.react

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.*
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.lastOrNull
import hep.dataforge.names.plus
import hep.dataforge.values.Value
import hep.dataforge.vision.hidden
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

public external interface PropertyEditorProps : RProps {

    /**
     * Root config object - always non null
     */
    public var ownProperties: MutableItemProvider

    /**
     * Provide default item (greyed out if used)
     */
    public var allProperties: ItemProvider?

    /**
     * Full path to the displayed node in [ownProperties]. Could be empty
     */
    public var name: Name

    /**
     * Root descriptor
     */
    public var descriptor: NodeDescriptor?


    /**
     * A coroutine scope for updates
     */
    public var scope: CoroutineScope?

    /**
     * Flow names of updated properties
     */
    public var updateFlow: Flow<Name>?
}

private val PropertyEditorItem: FunctionalComponent<PropertyEditorProps> =
    functionalComponent("ConfigEditorItem") { props ->
        propertyEditorItem(props)
    }

private fun RBuilder.propertyEditorItem(props: PropertyEditorProps) {
    var expanded: Boolean by useState { true }
    val descriptorItem: ItemDescriptor? = props.descriptor?.get(props.name)
    var ownProperty: MetaItem? by useState { props.ownProperties.getItem(props.name) }
    val actualItem: MetaItem? = props.allProperties?.getItem(props.name)

    val token = props.name.lastOrNull()?.toString() ?: "Properties"

    fun update() {
        ownProperty = props.ownProperties.getItem(props.name)
    }

    if (props.updateFlow != null) {
        useEffectWithCleanup(listOf(props.ownProperties, props.updateFlow)) {
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
            props.ownProperties.remove(props.name)
        } else {
            props.ownProperties[props.name] = it
        }
        update()
    }

    val removeClick: (Event) -> Unit = {
        props.ownProperties.remove(props.name)
        update()
    }

    if (actualItem is NodeItem) {
        val keys = buildSet {
            (descriptorItem as? NodeDescriptor)?.items?.filterNot {
                it.key.startsWith("@")  || it.value.hidden
            }?.forEach {
                add(NameToken(it.key))
            }
            ownProperty?.node?.items?.keys?.filterNot { it.body.startsWith("@") }?.let { addAll(it) }
        }
        // Do not show nodes without visible children
        if (keys.isEmpty()) return

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
                    if (ownProperty == null) {
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
                keys.forEach { token ->
                    styledLi {
                        css {
                            +TreeStyles.treeItem
                        }
                        child(PropertyEditorItem) {
                            attrs {
                                this.key = props.name.toString()
                                this.ownProperties = props.ownProperties
                                this.allProperties = props.allProperties
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
                        if (ownProperty == null) {
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
                    if (ownProperty == null) {
                        disabled = true
                    } else {
                        onClickFunction = removeClick
                    }
                }
            }

        }
    }
}


@JsExport
public val PropertyEditor: FunctionalComponent<PropertyEditorProps> = functionalComponent("PropertyEditor") { props ->
    child(PropertyEditorItem) {
        attrs {
            this.key = ""
            this.ownProperties = props.ownProperties
            this.allProperties = props.allProperties
            this.name = Name.EMPTY
            this.descriptor = props.descriptor
            this.scope = props.scope
        }
    }
}

public fun RBuilder.propertyEditor(
    ownProperties: MutableItemProvider,
    allProperties: ItemProvider?,
    updateFlow: Flow<Name>? = null,
    descriptor: NodeDescriptor? = null,
    scope: CoroutineScope? = null,
    key: Any? = null,
) {
    child(PropertyEditor) {
        attrs {
            this.ownProperties = ownProperties
            this.allProperties = allProperties
            this.updateFlow = updateFlow
            this.descriptor = descriptor
            this.key = key?.toString() ?: ""
            this.scope = scope
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
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


public fun RBuilder.configEditor(
    config: Config,
    default: ItemProvider? = null,
    descriptor: NodeDescriptor? = null,
    key: Any? = null,
    scope: CoroutineScope? = null,
): Unit = propertyEditor(config, default, config.flowUpdates(), descriptor, scope, key = key)

public fun Element.configEditor(
    config: Config,
    descriptor: NodeDescriptor? = null,
    default: Meta? = null,
    key: Any? = null,
    scope: CoroutineScope? = null,
): Unit = render(this) {
    configEditor(config, default, descriptor, key, scope)
}