package space.kscience.visionforge.react

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.css.properties.TextDecoration
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import react.*
import react.dom.attrs
import react.dom.render
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.ValueRequirement
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.names.*
import space.kscience.dataforge.values.Value
import space.kscience.visionforge.hidden
import styled.css
import styled.styledButton
import styled.styledDiv
import styled.styledSpan

public external interface PropertyEditorProps : RProps {

    /**
     * Root config object - always non null
     */
    public var ownProperties: MutableMetaProvider

    /**
     * Provide default item (greyed out if used)
     */
    public var allProperties: MetaProvider?

    /**
     * Full path to the displayed node in [ownProperties]. Could be empty
     */
    public var name: Name

    /**
     * Root descriptor
     */
    public var descriptor: MetaDescriptor?

    /**
     * A coroutine scope for updates
     */
    public var scope: CoroutineScope?

    /**
     * Flow names of updated properties
     */
    public var updateFlow: Flow<Name>?

    /**
     * Initial expanded state
     */
    public var expanded: Boolean?
}

private val PropertyEditorItem: FunctionComponent<PropertyEditorProps> =
    functionalComponent("ConfigEditorItem") { props ->
        propertyEditorItem(props)
    }

private fun RBuilder.propertyEditorItem(props: PropertyEditorProps) {
    var expanded: Boolean by useState { props.expanded ?: true }
    val descriptor: MetaDescriptor? = props.descriptor?.get(props.name)
    var ownProperty: Meta? by useState { props.ownProperties.getMeta(props.name) }
    val actualMeta = props.allProperties?.getMeta(props.name)

    val token = props.name.lastOrNull()?.toString() ?: "Properties"

    fun update() {
        ownProperty = props.ownProperties.getMeta(props.name)
    }

    if (props.updateFlow != null) {
        useEffect(props.ownProperties, props.updateFlow) {
            val updateJob = props.updateFlow!!.onEach { updatedName ->
                if (updatedName == props.name) {
                    update()
                }
            }.launchIn(props.scope ?: GlobalScope)
            cleanup {
                updateJob.cancel()
            }
        }
    }

    val expanderClick: (Event) -> Unit = {
        expanded = !expanded
    }

    val valueChanged: (Value?) -> Unit = {
        if (it == null) {
            props.ownProperties.remove(props.name)
        } else {
            props.ownProperties.setValue(props.name, it)
        }
        update()
    }

    val removeClick: (Event) -> Unit = {
        props.ownProperties.remove(props.name)
        update()
    }

    val keys = buildSet {
        descriptor?.children?.filterNot {
            it.key.startsWith("@") || it.value.hidden
        }?.forEach {
            add(NameToken(it.key))
        }
        //ownProperty?.items?.keys?.filterNot { it.body.startsWith("@") }?.let { addAll(it) }
    }

    flexRow {
        css {
            alignItems = Align.center
        }
        if(keys.isNotEmpty()) {
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
        if(!props.name.isEmpty() && descriptor?.valueRequirement != ValueRequirement.ABSENT) {
            styledDiv {
                css {
                    //+TreeStyles.resizeableInput
                    width = 160.px
                    margin(1.px, 5.px)
                }
                valueChooser(
                    props.name,
                    actualMeta,
                    descriptor,
                    valueChanged
                )
            }

            styledButton {
                css {
                    width = 24.px
                    alignSelf = Align.stretch
                    margin(1.px, 5.px)
                    backgroundColor = Color.white
                    borderStyle = BorderStyle.solid
                    borderRadius = 2.px
                    textAlign = TextAlign.center
                    textDecoration = TextDecoration.none
                    cursor = Cursor.pointer
                    disabled {
                        cursor = Cursor.auto
                        borderStyle = BorderStyle.dashed
                        color = Color.lightGray
                    }
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
    if (expanded) {
        flexColumn {
            css {
                +TreeStyles.tree
            }
            keys.forEach { token ->
                styledDiv {
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

}


@JsExport
public val PropertyEditor: FunctionComponent<PropertyEditorProps> = functionalComponent("PropertyEditor") { props ->
    child(PropertyEditorItem) {
        attrs {
            this.key = ""
            this.ownProperties = props.ownProperties
            this.allProperties = props.allProperties
            this.name = Name.EMPTY
            this.descriptor = props.descriptor
            this.scope = props.scope
            this.expanded = props.expanded
        }
    }
}

public fun RBuilder.propertyEditor(
    ownProperties: MutableMetaProvider,
    allProperties: MetaProvider? = ownProperties,
    updateFlow: Flow<Name>? = null,
    descriptor: MetaDescriptor? = null,
    scope: CoroutineScope? = null,
    key: Any? = null,
    expanded: Boolean? = null
) {
    child(PropertyEditor) {
        attrs {
            this.ownProperties = ownProperties
            this.allProperties = allProperties
            this.updateFlow = updateFlow
            this.descriptor = descriptor
            this.key = key?.toString() ?: ""
            this.scope = scope
            this.expanded = expanded
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun ObservableMutableMeta.flowUpdates(): Flow<Name> = callbackFlow {
    onChange(this) { name ->
        launch {
            send(name)
        }
    }
    awaitClose {
        removeListener(this)
    }
}


public fun RBuilder.configEditor(
    config: ObservableMutableMeta,
    default: MetaProvider? = null,
    descriptor: MetaDescriptor? = null,
    key: Any? = null,
    scope: CoroutineScope? = null,
): Unit = propertyEditor(config, default, config.flowUpdates(), descriptor, scope, key = key)

public fun Element.configEditor(
    config: ObservableMutableMeta,
    descriptor: MetaDescriptor? = null,
    default: Meta? = null,
    key: Any? = null,
    scope: CoroutineScope? = null,
): Unit = render(this) {
    configEditor(config, default, descriptor, key, scope)
}