package space.kscience.visionforge.react

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.css.properties.TextDecoration
import kotlinx.html.js.onClickFunction
import kotlinx.html.org.w3c.dom.events.Event
import react.*
import react.dom.attrs
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.ObservableMutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.ValueRequirement
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.remove
import space.kscience.dataforge.names.*
import space.kscience.visionforge.hidden
import styled.css
import styled.styledButton
import styled.styledDiv
import styled.styledSpan

/**
 * The display state of a property
 */
public sealed class EditorPropertyState {
    public object Defined : EditorPropertyState()
    public class Default(public val source: String = "unknown") : EditorPropertyState()

    public object Undefined : EditorPropertyState()

}


public external interface PropertyEditorProps : Props {

    /**
     * Root config object - always non-null
     */
    public var meta: MutableMeta

    public var getPropertyState: (Name) -> EditorPropertyState

    public var scope: CoroutineScope

    public var updates: Flow<Name>

    /**
     * Full path to the displayed node in [meta]. Could be empty
     */
    public var name: Name

    /**
     * Root descriptor
     */
    public var descriptor: MetaDescriptor?

    /**
     * Initial expanded state
     */
    public var expanded: Boolean?
}

private val PropertyEditorItem: FC<PropertyEditorProps> = fc("PropertyEditorItem") { props ->
    propertyEditorItem(props)
}

private fun RBuilder.propertyEditorItem(props: PropertyEditorProps) {
    var expanded: Boolean by useState { props.expanded ?: true }
    val descriptor: MetaDescriptor? = useMemo(props.descriptor, props.name) { props.descriptor?.get(props.name) }
    var property: MutableMeta by useState { props.meta.getOrCreate(props.name) }
    var editorPropertyState: EditorPropertyState by useState { props.getPropertyState(props.name) }


    val keys = useMemo(descriptor) {
        buildSet {
            descriptor?.children?.filterNot {
                it.key.startsWith("@") || it.value.hidden
            }?.forEach {
                add(NameToken(it.key))
            }
            //ownProperty?.items?.keys?.filterNot { it.body.startsWith("@") }?.let { addAll(it) }
        }
    }

    val token = props.name.lastOrNull()?.toString() ?: "Properties"

    fun update() {
        property = props.meta.getOrCreate(props.name)
        editorPropertyState = props.getPropertyState(props.name)
    }

    useEffect(props.meta) {
        val job = props.updates.onEach { updatedName ->
            if (updatedName == props.name) {
                update()
            }
        }.launchIn(props.scope)

        cleanup {
            job.cancel()
        }
    }

    val expanderClick: (Event) -> Unit = {
        expanded = !expanded
    }

    val removeClick: (Event) -> Unit = {
        props.meta.remove(props.name)
        update()
    }



    flexRow {
        css {
            alignItems = Align.center
        }
        if (keys.isNotEmpty()) {
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
                if (editorPropertyState != EditorPropertyState.Defined) {
                    +TreeStyles.treeLabelInactive
                }
            }
            +token
        }
        if (!props.name.isEmpty() && descriptor?.valueRequirement != ValueRequirement.ABSENT) {
            styledDiv {
                css {
                    //+TreeStyles.resizeableInput
                    width = 160.px
                    margin(1.px, 5.px)
                }
                ValueChooser {
                    attrs {
                        this.descriptor = descriptor
                        this.state = editorPropertyState
                        this.value = property.value
                        this.onValueChange = {
                            property.value = it
                            editorPropertyState = props.getPropertyState(props.name)
                        }
                    }
                }
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
                    if (editorPropertyState!= EditorPropertyState.Defined) {
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
                            this.meta = props.meta
                            this.name = props.name + token
                            this.descriptor = props.descriptor
                            this.scope = props.scope
                            this.getPropertyState = { props.getPropertyState(props.name + token) }
                            this.updates = props.updates
                        }
                    }
                    //configEditor(props.root, props.name + token, props.descriptor, props.default)
                }
            }
        }
    }
}

@JsExport
public val PropertyEditor: FC<PropertyEditorProps> = fc("PropertyEditor") { props ->
    child(PropertyEditorItem) {
        attrs {
            this.key = ""
            this.meta = props.meta
            this.name = Name.EMPTY
            this.descriptor = props.descriptor
            this.expanded = props.expanded
            this.scope = props.scope
            this.getPropertyState = props.getPropertyState
            this.updates = props.updates
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
public fun RBuilder.propertyEditor(
    scope: CoroutineScope,
    properties: ObservableMutableMeta,
    descriptor: MetaDescriptor? = null,
    key: Any? = null,
    expanded: Boolean? = null,
) {
    child(PropertyEditor) {
        attrs {
            this.meta = properties
            this.descriptor = descriptor
            this.key = key?.toString() ?: ""
            this.expanded = expanded
            this.scope = scope
            this.getPropertyState = { name ->
                if (properties[name] != null) {
                    EditorPropertyState.Defined
                } else if (descriptor?.get(name)?.defaultValue != null) {
                    EditorPropertyState.Default("descriptor")
                } else {
                    EditorPropertyState.Undefined
                }
            }
            this.updates = callbackFlow {
                properties.onChange(scope) { name ->
                    scope.launch {
                        send(name)
                    }
                }

                invokeOnClose {
                    properties.removeListener(scope)
                }
            }
        }
    }
}