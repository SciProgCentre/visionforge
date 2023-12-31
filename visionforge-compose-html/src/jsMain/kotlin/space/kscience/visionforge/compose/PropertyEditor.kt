package space.kscience.visionforge.compose

import androidx.compose.runtime.*
import app.softwork.bootstrapcompose.CloseButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.ObservableMutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.ValueRestriction
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.meta.remove
import space.kscience.dataforge.names.*
import space.kscience.visionforge.hidden


/**
 * The display state of a property
 */
public sealed class EditorPropertyState {
    public data object Defined : EditorPropertyState()
    public data class Default(public val source: String = "unknown") : EditorPropertyState()
    public data object Undefined : EditorPropertyState()

}

/**
 * @param rootMeta Root config object - always non-null
 * @param rootDescriptor Full path to the displayed node in [rootMeta]. Could be empty
 */
@Composable
public fun PropertyEditor(
    scope: CoroutineScope,
    rootMeta: MutableMeta,
    getPropertyState: (Name) -> EditorPropertyState,
    updates: Flow<Name>,
    name: Name = Name.EMPTY,
    rootDescriptor: MetaDescriptor? = null,
    initialExpanded: Boolean? = null,
) {
    var expanded: Boolean by remember { mutableStateOf(initialExpanded ?: true) }
    val descriptor: MetaDescriptor? = remember(rootDescriptor, name) { rootDescriptor?.get(name) }
    var property: MutableMeta by remember { mutableStateOf(rootMeta.getOrCreate(name)) }
    var editorPropertyState: EditorPropertyState by remember { mutableStateOf(getPropertyState(name)) }


    val keys by derivedStateOf {
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
        property = rootMeta.getOrCreate(name)
        editorPropertyState = getPropertyState(name)
    }

    LaunchedEffect(rootMeta) {
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

        if (!name.isEmpty() && descriptor?.valueRestriction != ValueRestriction.ABSENT) {
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

            CloseButton(editorPropertyState != EditorPropertyState.Defined) {
                rootMeta.remove(name)
                update()
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
                    PropertyEditor(scope, rootMeta, getPropertyState, updates, name + token, rootDescriptor, expanded)
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
        rootMeta = properties,
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

            awaitClose { properties.removeListener(scope) }
        },
        name = Name.EMPTY,
        rootDescriptor = descriptor,
        initialExpanded = expanded,
    )
}