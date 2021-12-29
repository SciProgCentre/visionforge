package space.kscience.visionforge.react

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
import space.kscience.visionforge.hidden
import styled.css
import styled.styledButton
import styled.styledDiv
import styled.styledSpan

public external interface PropertyEditorProps : Props {

    /**
     * Root config object - always non-null
     */
    public var meta: ObservableMutableMeta

    /**
     * Provide default item (greyed out if used)
     */
    public var withDefault: MetaProvider

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
    var ownProperty: ObservableMutableMeta by useState { props.meta.getOrCreate(props.name) }

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
        ownProperty = props.meta.getOrCreate(props.name)
    }

    useEffect(props.meta) {
        props.meta.onChange(props) { updatedName ->
            if (updatedName == props.name) {
                update()
            }
        }
        cleanup {
            props.meta.removeListener(props)
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
                if (ownProperty.isEmpty()) {
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
                        this.meta = ownProperty
                        this.actual = props.withDefault.getMeta(props.name) ?: ownProperty
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
                    if (ownProperty.isEmpty()) {
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
                            this.withDefault = props.withDefault
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
public val PropertyEditor: FC<PropertyEditorProps> = fc("PropertyEditor") { props ->
    child(PropertyEditorItem) {
        attrs {
            this.key = ""
            this.meta = props.meta
            this.withDefault = props.withDefault
            this.name = Name.EMPTY
            this.descriptor = props.descriptor
            this.expanded = props.expanded
        }
    }
}

public fun RBuilder.propertyEditor(
    ownProperties: ObservableMutableMeta,
    allProperties: MetaProvider = ownProperties,
    descriptor: MetaDescriptor? = null,
    key: Any? = null,
    expanded: Boolean? = null,
) {
    child(PropertyEditor) {
        attrs {
            this.meta = ownProperties
            this.withDefault = allProperties
            this.descriptor = descriptor
            this.key = key?.toString() ?: ""
            this.expanded = expanded
        }
    }
}

public fun RBuilder.configEditor(
    config: ObservableMutableMeta,
    default: MetaProvider = config,
    descriptor: MetaDescriptor? = null,
    key: Any? = null,
): Unit = propertyEditor(config, default, descriptor, key = key)

public fun Element.configEditor(
    config: ObservableMutableMeta,
    default: Meta = config,
    descriptor: MetaDescriptor? = null,
    key: Any? = null,
): Unit = render(this) {
    configEditor(config, default, descriptor, key = key)
}