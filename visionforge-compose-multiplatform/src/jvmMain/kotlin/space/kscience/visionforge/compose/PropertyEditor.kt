package space.kscience.visionforge.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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
    rootMeta: MutableMeta,
    getPropertyState: (Name) -> EditorPropertyState,
    updates: Flow<Name>,
    name: Name,
    rootDescriptor: MetaDescriptor?,
    initialExpanded: Boolean? = null,
) {
    var expanded: Boolean by remember { mutableStateOf(initialExpanded ?: true) }
    val descriptor: MetaDescriptor? by derivedStateOf { rootDescriptor?.get(name) }
    var displayedValue by remember { mutableStateOf(rootMeta.getValue(name)) }
    var editorPropertyState: EditorPropertyState by remember { mutableStateOf(getPropertyState(name)) }

    fun buildKeys() = buildSet {
        descriptor?.nodes?.filterNot {
            it.key.startsWith("@") || it.value.hidden
        }?.forEach {
            add(NameToken(it.key))
        }
        rootMeta[name]?.items?.keys?.filterNot { it.body.startsWith("@") }?.let { addAll(it) }
    }

    var keys by remember { mutableStateOf(buildKeys()) }

    val token = name.lastOrNull()?.toString() ?: "Properties"

    fun update() {
        displayedValue = rootMeta.getValue(name)
        editorPropertyState = getPropertyState(name)
        keys = buildKeys()
    }

    LaunchedEffect(rootMeta) {
        updates.collect { updatedName ->
            if (name.startsWith(updatedName)) {
                update()
            }
        }
    }
    Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(start = 20.dp)) {
            //if node has children
            if (keys.isNotEmpty()) {
                TextButton(
                    { expanded = !expanded },
                    modifier = Modifier.align(Alignment.CenterVertically).width(40.dp)
                ) {
                    if (expanded) {
                        Icon(Icons.Filled.ExpandLess, "collapse")
                    } else {
                        Icon(Icons.Filled.ExpandMore, "expand")
                    }
                }
            }
            Text(
                token,
                color = when (editorPropertyState) {
                    is EditorPropertyState.Default, EditorPropertyState.Undefined -> Color.Gray
                    else -> Color.Unspecified
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.weight(1f))

            if (!name.isEmpty() && descriptor?.valueRestriction != ValueRestriction.ABSENT) {
                Box(modifier = Modifier.padding(1.dp, 5.dp).width(160.dp)) {
                    ValueChooser(descriptor, editorPropertyState, displayedValue) {
                        rootMeta.setValue(name, it)
                        update()
                    }
                }

            }
            if (!name.isEmpty()) {
                TextButton(
                    onClick = {
                        rootMeta.remove(name)
                        update()
                    },
                    enabled = editorPropertyState == EditorPropertyState.Defined,
                    modifier = Modifier.align(Alignment.CenterVertically).width(50.dp)
                ) {
                    if (editorPropertyState == EditorPropertyState.Defined) {
                        Icon(Icons.Filled.Clear, "Reset")
                    }
                }
            }
        }
        if (expanded) {
            Column(modifier = Modifier.fillMaxWidth()) {
                keys.forEach { token ->
                    PropertyEditor(rootMeta, getPropertyState, updates, name + token, rootDescriptor, expanded)
                }
            }
        }
    }
}


@Composable
public fun PropertyEditor(
    properties: ObservableMutableMeta,
    descriptor: MetaDescriptor? = null,
    expanded: Boolean? = null,
) {
    val scope = rememberCoroutineScope()
    PropertyEditor(
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