package space.kscience.visionforge.compose

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.ObservableMutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.ValueRequirement
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.remove
import space.kscience.dataforge.names.*
import space.kscience.visionforge.hidden


/**
 * The display state of a property
 */
public sealed class EditorPropertyState {
    public object Defined : EditorPropertyState()
    public class Default(public val source: String = "unknown") : EditorPropertyState()
    public object Undefined : EditorPropertyState()

}

/**
 * @param meta Root config object - always non-null
 * @param rootDescriptor Full path to the displayed node in [meta]. Could be empty
 */
@Composable
public fun PropertyEditor(
    scope: CoroutineScope,
    meta: MutableMeta,
    getPropertyState: (Name) -> EditorPropertyState,
    updates: Flow<Name>,
    name: Name = Name.EMPTY,
    rootDescriptor: MetaDescriptor? = null,
    initialExpanded: Boolean? = null,
) {
    var expanded: Boolean by remember { mutableStateOf(initialExpanded ?: true) }
    val descriptor: MetaDescriptor? = remember(rootDescriptor, name) { rootDescriptor?.get(name) }
    var property: MutableMeta by remember { mutableStateOf(meta.getOrCreate(name)) }
    var editorPropertyState: EditorPropertyState by remember { mutableStateOf(getPropertyState(name)) }


    val keys = remember(descriptor) {
        buildSet {
            descriptor?.children?.filterNot {
                it.key.startsWith("@") || it.value.hidden
            }?.forEach {
                add(NameToken(it.key))
            }
            //ownProperty?.items?.keys?.filterNot { it.body.startsWith("@") }?.let { addAll(it) }
        }
    }

    val token = name.lastOrNull()?.toString() ?: "Properties"

    fun update() {
        property = meta.getOrCreate(name)
        editorPropertyState = getPropertyState(name)
    }

    LaunchedEffect(meta) {
        updates.collect { updatedName ->
            if (updatedName == name) {
                update()
            }
        }
    }

    FlexRow({
        style {
            alignItems(AlignItems.Center)
        }
    }) {
        if (keys.isNotEmpty()) {
            Span({
                classes(TreeStyles.treeCaret)
                if (expanded) {
                    classes(TreeStyles.treeCaretDown)
                }
                onClick { expanded = !expanded }
            })
        }
        Span({
            classes(TreeStyles.treeLabel)
            if (editorPropertyState != EditorPropertyState.Defined) {
                classes(TreeStyles.treeLabelInactive)
            }
        }) {
            Text(token)
        }

        if (!name.isEmpty() && descriptor?.valueRequirement != ValueRequirement.ABSENT) {
            Div({
                style {
                    width(160.px)
                    marginAll(1.px, 5.px)
                }
            }) {
                ValueChooser(descriptor, editorPropertyState, property.value) {
                    property.value = it
                    editorPropertyState = getPropertyState(name)
                }
            }

            Button({
                classes(TreeStyles.propertyEditorButton)
                if (editorPropertyState != EditorPropertyState.Defined) {
                    disabled()
                } else {
                    onClick {
                        meta.remove(name)
                        update()
                    }
                }
            }) {
                Text("\u00D7")
            }
        }
    }
    if (expanded) {
        FlexColumn({
            classes(TreeStyles.tree)
        }) {
            keys.forEach { token ->
                Div({
                    classes(TreeStyles.treeItem)
                }) {
                    PropertyEditor(scope, meta, getPropertyState, updates, name + token, descriptor, expanded)
                }
            }
        }
    }
}

@Composable
public fun PropertyEditor(
    scope: CoroutineScope,
    properties: ObservableMutableMeta,
    descriptor: MetaDescriptor? = null,
    expanded: Boolean? = null,
) {
    PropertyEditor(
        scope = scope,
        meta = properties,
        getPropertyState = { name ->
            if (properties[name] != null) {
                EditorPropertyState.Defined
            } else if (descriptor?.get(name)?.defaultValue != null) {
                EditorPropertyState.Default("descriptor")
            } else {
                EditorPropertyState.Undefined
            }
        },
        updates = callbackFlow {
            properties.onChange(scope) { name ->
                scope.launch {
                    send(name)
                }
            }

            invokeOnClose {
                properties.removeListener(scope)
            }
        },
        name = Name.EMPTY,
        rootDescriptor = descriptor,
        initialExpanded = expanded,
    )
}