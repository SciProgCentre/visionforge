package space.kscience.visionforge.solid.three.compose

import androidx.compose.runtime.*
import app.softwork.bootstrapcompose.Card
import app.softwork.bootstrapcompose.Column
import app.softwork.bootstrapcompose.Layout.Height
import app.softwork.bootstrapcompose.Layout.Width
import app.softwork.bootstrapcompose.Row
import kotlinx.dom.clear
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.request
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.isEmpty
import space.kscience.visionforge.Vision
import space.kscience.visionforge.compose.*
import space.kscience.visionforge.root
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.SolidGroup
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import space.kscience.visionforge.solid.three.ThreeCanvas
import space.kscience.visionforge.solid.three.ThreePlugin
import space.kscience.visionforge.styles

@Composable
private fun SimpleThreeView(
    context: Context,
    options: Canvas3DOptions?,
    solid: Solid?,
    selected: Name?,
) {

    Div({
        style {
            maxWidth(100.vw)
            maxHeight(100.vh)
            width(100.percent)
            height(100.percent)
        }
    }) {
        var canvas: ThreeCanvas? by remember { mutableStateOf(null) }
        DisposableEffect(options) {
            canvas = ThreeCanvas(context.request(ThreePlugin), scopeElement, options ?: Canvas3DOptions())
            onDispose {
                scopeElement.clear()
                canvas = null
            }
        }
        LaunchedEffect(solid) {
            if (solid != null) {
                canvas?.render(solid)
            } else {
                canvas?.clear()
            }
        }
        LaunchedEffect(selected) {
            canvas?.select(selected)
        }
    }
}


@Composable
public fun ThreeView(
    solids: Solids,
    solid: Solid?,
    initialSelected: Name? = null,
    options: Canvas3DOptions? = null,
    sidebarTabs: @Composable TabsBuilder.() -> Unit = {},
) {
    var selected: Name? by remember { mutableStateOf(initialSelected) }

    val optionsSnapshot by derivedStateOf {
        (options ?: Canvas3DOptions()).apply {
            this.onSelect = {
                selected = it
            }
        }
    }

    val selectedVision: Vision? by derivedStateOf {
        selected?.let {
            when {
                it.isEmpty() -> solid
                else -> (solid as? SolidGroup)?.get(it)
            }
        }
    }

    if (optionsSnapshot.controls.enabled) {

        Row(
            styling = {
                Layout {
                    width = Width.Full
                    height = Height.Full
                }
            }
        ) {
            Column(
                styling = {
                    Layout {
                        height = Height.Full
                    }
                },
                attrs = {
                    style {
                        position(Position.Relative)
                        minWidth(600.px)
                    }
                }
            ) {
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

                            @OptIn(ExperimentalComposeWebApi::class) filter {
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
                    SimpleThreeView(solids.context, optionsSnapshot, solid, selected)
                }

                selectedVision?.let { vision ->
                    Card(
                        attrs = {
                            style {
                                position(Position.Absolute)
                                top(5.px)
                                right(5.px)
                                width(450.px)
                            }
                        },
                        headerAttrs = {
                            // border = true
                        },
                        header = {
                            NameCrumbs(selected) { selected = it }
                        },
                        footer = {
                            vision.styles.takeIf { it.isNotEmpty() }?.let { styles ->
                                P {
                                    B { Text("Styles: ") }
                                    Text(styles.joinToString(separator = ", "))
                                }
                            }
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
                }
            }

            Column(
                auto = true,
                styling = {
                    Layout {
                        height = Height.Full
                    }
                },
                attrs = {
                    style {
                        paddingAll(4.px)
                        minWidth(400.px)
                        height(100.percent)
                        overflowY("auto")
                    }
                }
            ) {
                ThreeControls(solid, optionsSnapshot, selected, onSelect = { selected = it }, tabBuilder = sidebarTabs)
            }
        }
    } else {
        SimpleThreeView(solids.context, optionsSnapshot, solid, selected)
    }
}