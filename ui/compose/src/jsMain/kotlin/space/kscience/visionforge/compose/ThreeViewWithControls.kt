@file:OptIn(ExperimentalComposeWebApi::class)

package space.kscience.visionforge.compose

import androidx.compose.runtime.*
import app.softwork.bootstrapcompose.Card
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.isEmpty
import space.kscience.visionforge.*
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.SolidGroup
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.specifications.Canvas3DOptions

@Composable
public fun ThreeCanvasWithControls(
    solids: Solids,
    builderOfSolid: Deferred<Solid?>,
    initialSelected: Name?,
    options: Canvas3DOptions?,
    tabBuilder: @Composable TabsBuilder.() -> Unit = {},
) {
    var selected: Name? by remember { mutableStateOf(initialSelected) }
    var solid: Solid? by remember { mutableStateOf(null) }

    LaunchedEffect(builderOfSolid) {
        solids.context.launch {
            solid = builderOfSolid.await()
            //ensure that the solid is properly rooted
            if (solid?.parent == null) {
                solid?.setAsRoot(solids.context.visionManager)
            }
        }
    }

    val optionsWithSelector = remember(options) {
        (options ?: Canvas3DOptions()).apply {
            this.onSelect = {
                selected = it
            }
        }
    }

    val selectedVision: Vision? = remember(builderOfSolid, selected) {
        selected?.let {
            when {
                it.isEmpty() -> solid
                else -> (solid as? SolidGroup)?.get(it)
            }
        }
    }


    FlexRow({
        style {
            height(100.percent)
            width(100.percent)
            flexWrap(FlexWrap.Wrap)
            alignItems(AlignItems.Stretch)
            alignContent(AlignContent.Stretch)
        }
    }) {
        FlexColumn({
            style {
                height(100.percent)
                minWidth(600.px)
                flex(10, 1, 600.px)
                position(Position.Relative)
            }
        }) {
            if (solid == null) {
                Div({
                    style {
                        position(Position.Fixed)
                        width(100.percent)
                        height(100.percent)
                        zIndex(1000)
                        top(40.percent)
                        left(0.px)
                        opacity(0.5)
                        filter {
                            opacity(50.percent)
                        }
                    }
                }) {
                    Div({ classes("d-flex", " justify-content-center") }) {
                        Div({
                            classes("spinner-grow", "text-primary")
                            style {
                                width(3.cssRem)
                                height(3.cssRem)
                                zIndex(20)
                            }
                            attr("role", "status")
                        }) {
                            Span({ classes("sr-only") }) { Text("Loading 3D vision") }
                        }
                    }
                }
            } else {
                ThreeCanvas(solids.context, optionsWithSelector, solid, selected)
            }

            selectedVision?.let { vision ->
                Div({
                    style {
                        position(Position.Absolute)
                        top(5.px)
                        right(5.px)
                        width(450.px)
                    }
                }) {
                    Card(
                        headerAttrs = {
                            // border = true
                        },
                        header = {
                            NameCrumbs(selected) { selected = it }
                        }
                    ) {
                        PropertyEditor(
                            scope = solids.context,
                            meta = vision.properties.root(),
                            getPropertyState = { name ->
                                if (vision.properties.own?.get(name) != null) {
                                    EditorPropertyState.Defined
                                } else if (vision.properties.root()[name] != null) {
                                    // TODO differentiate
                                    EditorPropertyState.Default()
                                } else {
                                    EditorPropertyState.Undefined
                                }
                            },
                            updates = vision.properties.changes,
                            rootDescriptor = vision.descriptor
                        )

                    }

                    vision.styles.takeIf { it.isNotEmpty() }?.let { styles ->
                        P {
                            B { Text("Styles: ") }
                            Text(styles.joinToString(separator = ", "))
                        }
                    }
                }
            }
        }
    }
    FlexColumn({
        style {
            paddingAll(4.px)
            minWidth(400.px)
            height(100.percent)
            overflowY("auto")
            flex(1, 10, 300.px)
        }
    }) {
        ThreeControls(solid, optionsWithSelector, selected, onSelect = { selected = it }, tabBuilder = tabBuilder)
    }
}


