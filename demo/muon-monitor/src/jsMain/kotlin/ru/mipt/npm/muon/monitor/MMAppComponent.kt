package ru.mipt.npm.muon.monitor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import app.softwork.bootstrapcompose.Button
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.fetch.RequestInit
import space.kscience.dataforge.meta.invoke
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Colors
import space.kscience.visionforge.compose.FlexColumn
import space.kscience.visionforge.compose.FlexRow
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.ambientLight
import space.kscience.visionforge.solid.edges
import space.kscience.visionforge.solid.invoke
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import space.kscience.visionforge.solid.three.compose.ThreeView
import kotlin.math.PI

@Composable
fun MMApp(solids: Solids, model: Model, selected: Name? = null) {

    val mmOptions = remember {
        Canvas3DOptions {
            camera {
                distance = 2100.0
                latitude = PI / 6
                azimuth = PI + PI / 6
            }
        }
    }

    val root = remember(model) {
        model.root.apply {
            edges()
            ambientLight {
                color(Colors.white)
            }
        }
    }

    val events = remember { mutableStateListOf<Event>() }

    Div({
        style {
            height(100.vh - 12.pt)
        }
    }) {
        ThreeView(solids, root, selected, mmOptions) {
            Tab("Events") {

                FlexColumn {
                    FlexRow {
                        Button("Next") {
                            solids.context.launch {
                                val event = window.fetch(
                                    "http://localhost:8080/event",
                                    RequestInit("GET")
                                ).then { response ->
                                    if (response.ok) {
                                        response.text()
                                    } else {
                                        error("Failed to get event")
                                    }
                                }.then { body ->
                                    Json.decodeFromString(Event.serializer(), body)
                                }.await()
                                events.add(event)
                                model.displayEvent(event)
                            }
                        }
                        Button("Clear") {
                            events.clear()
                            model.reset()
                        }
                    }
                }
                events.forEach { event ->
                    P {
                        Span {
                            Text(event.id.toString())
                        }
                        Text(" : ")
                        Span({
                            style {
                                color(Color.blue)
                            }
                        }) {
                            Text(event.hits.toString())
                        }
                    }
                }
            }
        }
    }
}