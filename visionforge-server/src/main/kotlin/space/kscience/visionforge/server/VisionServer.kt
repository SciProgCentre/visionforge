package space.kscience.visionforge.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.server.websocket.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.html.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.*
import space.kscience.visionforge.html.*
import kotlin.time.Duration.Companion.milliseconds


public class VisionRoute(
    public val route: String,
    public val visionManager: VisionManager,
    override val meta: ObservableMutableMeta = MutableMeta(),
) : Configurable, ContextAware {

    public enum class Mode {
        /**
         * Embed the initial state of the vision inside its html tag.
         */
        EMBED,

        /**
         * Fetch data on vision load. Do not embed data.
         */
        FETCH,

        /**
         * Connect to server to get pushes. The address of the server is embedded in the tag.
         */
        UPDATE
    }

    override val context: Context get() = visionManager.context

    /**
     * Update the minimal interval between updates in milliseconds (if there are no updates, push will not happen
     */
    public var updateInterval: Long by meta.long(300, key = UPDATE_INTERVAL_KEY)

    public var dataMode: Mode by meta.enum(Mode.UPDATE)

    public companion object {
        public const val DEFAULT_PORT: Int = 7777
        public const val DEFAULT_PAGE: String = "/"
        public val UPDATE_INTERVAL_KEY: Name = Name.parse("update.interval")
    }
}


/**
 * Serve visions in a given [route] without providing a page template.
 * [visions] could be changed during the service.
 */
public fun Application.serveVisionData(
    configuration: VisionRoute,
    onEvent: suspend Vision.(VisionEvent) -> Unit = { event ->
        if (event is VisionChangeEvent) {
            update(event.change)
        }
    },
    resolveVision: (Name) -> Vision?,
) {
    require(WebSockets)
    routing {
        route(configuration.route) {
            install(CORS) {
                anyHost()
            }
            application.log.info("Serving visions at ${configuration.route}")

            //Update websocket
            webSocket("ws") {
                val name: String = call.request.queryParameters.getOrFail("name")
                application.log.debug("Opened server socket for $name")
                val vision: Vision = resolveVision(Name.parse(name)) ?: error("Vision with id='$name' not registered")

                launch {
                    for (frame in incoming) {
                        val data = frame.data.decodeToString()
                        application.log.debug("Received event for $name: \n$data")
                        val event = configuration.visionManager.jsonFormat.decodeFromString(
                            VisionEvent.serializer(), data
                        )
                        vision.onEvent(event)
                    }
                }

                try {
                    withContext(configuration.context.coroutineContext) {
                        vision.flowChanges(configuration.updateInterval.milliseconds).onEach { update ->
                            val json = configuration.visionManager.jsonFormat.encodeToString(
                                VisionChange.serializer(),
                                update
                            )
                            application.log.debug("Sending update for $name: \n$json")
                            outgoing.send(Frame.Text(json))
                        }.collect()
                    }
                } catch (t: Throwable) {
                    this.application.log.info("WebSocket update channel for $name is closed with exception: $t")
                }
            }
            //Plots in their json representation
            get("data") {
                val name: String = call.request.queryParameters.getOrFail("name")

                val vision: Vision? = resolveVision(Name.parse(name))
                if (vision == null) {
                    call.respond(HttpStatusCode.NotFound, "Vision with name '$name' not found")
                } else {
                    call.respondText(
                        configuration.visionManager.encodeToString(vision),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK
                    )
                }
            }
        }
    }
}

public fun Application.serveVisionData(
    configuration: VisionRoute,
    data: Map<Name, Vision>,
): Unit = serveVisionData(configuration) { data[it] }

/**
 * Serve a page, potentially containing any number of visions at a given [route] with given [header].
 */
public fun Application.visionPage(
    route: String,
    configuration: VisionRoute,
    headers: Collection<HtmlFragment>,
    connector: EngineConnectorConfig? = null,
    visionFragment: HtmlVisionFragment,
) {
    require(WebSockets)

    val collector: MutableMap<Name, Vision> = mutableMapOf()

    //serve data
    serveVisionData(configuration, collector)

    //filled pages
    routing {
        get(route) {
            val host = connector?.host ?: call.request.host()
            val port = connector?.port ?: call.request.port()
            call.respondHtml {
                head {
                    meta {
                        charset = "utf-8"
                    }
                    headers.forEach { headerContent ->
                        headerContent.appendTo(consumer)
                    }
                }
                body {
                    //Load the fragment and remember all loaded visions
                    visionFragment(
                        visionManager = configuration.visionManager,
                        embedData = configuration.dataMode == VisionRoute.Mode.EMBED,
                        fetchDataUrl = if (configuration.dataMode != VisionRoute.Mode.EMBED) {
                            url {
                                this.host = host
                                this.port = port
                                path(route, "data")
                            }
                        } else null,
                        updatesUrl = if (configuration.dataMode == VisionRoute.Mode.UPDATE) {
                            url {
                                protocol = URLProtocol.WS
                                this.host = host
                                this.port = port
                                path(route, "ws")
                            }
                        } else null,
                        onVisionRendered = { name, vision -> collector[name] = vision },
                        fragment = visionFragment
                    )
                }
            }
        }
    }
}

public fun Application.visionPage(
    visionManager: VisionManager,
    vararg headers: HtmlFragment,
    route: String = "/",
    connector: EngineConnectorConfig? = null,
    configurationBuilder: VisionRoute.() -> Unit = {},
    visionFragment: HtmlVisionFragment,
) {
    val configuration = VisionRoute(route, visionManager).apply(configurationBuilder)
    visionPage(route, configuration, listOf(*headers), connector, visionFragment)
}

/**
 * Render given [VisionPage] at server
 */
public fun Application.visionPage(
    page: VisionPage,
    route: String = "/",
    connector: EngineConnectorConfig? = null,
    configurationBuilder: VisionRoute.() -> Unit = {},
) {
    val configuration = VisionRoute(route, page.visionManager).apply(configurationBuilder)
    visionPage(route, configuration, page.pageHeaders.values, connector, visionFragment = page.content)
}

