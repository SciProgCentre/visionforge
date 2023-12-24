package space.kscience.visionforge.server

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.EngineConnectorConfig
import io.ktor.server.html.respondHtml
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.header
import io.ktor.server.request.host
import io.ktor.server.request.port
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import io.ktor.server.util.url
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.application
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.meta
import kotlinx.serialization.encodeToString
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.meta.Configurable
import space.kscience.dataforge.meta.ObservableMutableMeta
import space.kscience.dataforge.meta.enum
import space.kscience.dataforge.meta.long
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionEvent
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.flowChanges
import space.kscience.visionforge.html.*
import kotlin.time.Duration.Companion.milliseconds


public class VisionRoute(
    public val route: String,
    public val visionManager: VisionManager,
    override val meta: ObservableMutableMeta = ObservableMutableMeta(),
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
 *
 * @return a [Flow] of backward events, including vision change events
 */
public fun Application.serveVisionData(
    configuration: VisionRoute,
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
                        val event: VisionEvent = configuration.visionManager.jsonFormat.decodeFromString(data)

                        vision.receiveEvent(event)
                    }
                }

                try {
                    withContext(configuration.context.coroutineContext) {
                        vision.flowChanges(configuration.updateInterval.milliseconds).onEach { event ->
                            val json = configuration.visionManager.jsonFormat.encodeToString<VisionEvent>(event)
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
    data: Map<Name, VisionDisplay>,
): Unit = serveVisionData(configuration) { data[it]?.vision }

/**
 * Serve a page, potentially containing any number of visions at a given [route] with given [header].
 *
 * @return a [Flow] containing backward propagated events, including vision change events
 */
public fun Application.visionPage(
    route: String,
    configuration: VisionRoute,
    headers: Collection<HtmlFragment>,
    connector: EngineConnectorConfig? = null,
    visionFragment: HtmlVisionFragment,
) {
    require(WebSockets)

    val cache: MutableMap<Name, VisionDisplay> = mutableMapOf()

    //serve data
    serveVisionData(configuration, cache)

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
                        displayCache =  cache,
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

