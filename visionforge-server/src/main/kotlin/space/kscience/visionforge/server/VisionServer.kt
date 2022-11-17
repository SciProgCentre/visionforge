package space.kscience.visionforge.server

import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionChange
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.flowChanges
import space.kscience.visionforge.html.HtmlFragment
import space.kscience.visionforge.html.HtmlVisionFragment
import space.kscience.visionforge.html.VisionPage
import space.kscience.visionforge.html.visionFragment
import space.kscience.visionforge.server.VisionServer.Companion.DEFAULT_PAGE
import java.awt.Desktop
import java.net.URI
import kotlin.time.Duration.Companion.milliseconds


public enum class DataServeMode {
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

/**
 * A ktor plugin container with given [routing]
 * @param serverUrl a server url including root route
 */
public class VisionServer internal constructor(
    private val visionManager: VisionManager,
    private val serverUrl: Url,
    private val root: Route,
) : Configurable {

    public val application: Application get() = root.application

    override val meta: ObservableMutableMeta = MutableMeta()

    /**
     * Update minimal interval between updates in milliseconds (if there are no updates, push will not happen
     */
    public var updateInterval: Long by meta.long(300, key = UPDATE_INTERVAL_KEY)

    /**
     * Cache page fragments. If false, pages will be reconstructed on each call. Default: `true`
     */
    public var cacheFragments: Boolean by meta.boolean(true)

    public var dataMode: DataServeMode by meta.enum(DataServeMode.UPDATE)

    private val serverHeaders: MutableMap<String, HtmlFragment> = mutableMapOf()

    /**
     * Set up a default header that is automatically added to all pages on this server
     */
    public fun header(key: String, block: HtmlFragment) {
        serverHeaders[key] = block
    }

    private fun HTML.visionPage(
        pagePath: String,
        headers: Map<String, HtmlFragment>,
        visionFragment: HtmlVisionFragment,
    ): Map<Name, Vision> {
        var visionMap: Map<Name, Vision>? = null

        head {
            meta {
                charset = "utf-8"
            }
            (serverHeaders + headers).values.forEach {
                consumer.it()
            }
        }
        body {
            //Load the fragment and remember all loaded visions
            visionMap = visionFragment(
                context = visionManager.context,
                embedData = dataMode == DataServeMode.EMBED,
                fetchDataUrl = if (dataMode != DataServeMode.EMBED) "$serverUrl$pagePath/data" else null,
                fetchUpdatesUrl = if (dataMode == DataServeMode.UPDATE) "$serverUrl$pagePath/ws" else null,
                fragment = visionFragment
            )
        }

        return visionMap!!
    }

    /**
     * Server a map of visions without providing explicit html page for them
     */
    private fun serveVisions(route: Route, visions: Map<Name, Vision>): Unit = route {
        application.log.info("Serving visions $visions at $route")

        //Update websocket
        webSocket("ws") {
            val name: String = call.request.queryParameters.getOrFail("name")
            application.log.debug("Opened server socket for $name")
            val vision: Vision = visions[Name.parse(name)] ?: error("Plot with id='$name' not registered")

            launch {
                incoming.consumeEach {
                    val data = it.data.decodeToString()
                    application.log.debug("Received update: \n$data")
                    val change = visionManager.jsonFormat.decodeFromString(
                        VisionChange.serializer(), data
                    )
                    vision.update(change)
                }
            }

            try {
                withContext(visionManager.context.coroutineContext) {
                    vision.flowChanges(updateInterval.milliseconds).onEach { update ->
                        val json = visionManager.jsonFormat.encodeToString(
                            VisionChange.serializer(),
                            update
                        )
                        application.log.debug("Sending update: \n$json")
                        outgoing.send(Frame.Text(json))
                    }.collect()
                }
            } catch (t: Throwable) {
                application.log.info("WebSocket update channel for $name is closed with exception: $t")
            }
        }
        //Plots in their json representation
        get("data") {
            val name: String = call.request.queryParameters.getOrFail("name")

            val vision: Vision? = visions[Name.parse(name)]
            if (vision == null) {
                call.respond(HttpStatusCode.NotFound, "Vision with name '$name' not found")
            } else {
                call.respondText(
                    visionManager.encodeToString(vision),
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.OK
                )
            }
        }
    }


    /**
     * Serve visions in a given [route] without providing a page template
     */
    public fun serveVisions(route: String, visions: Map<Name, Vision>) {
        root.route(route) {
            serveVisions(this, visions)
        }
    }

    /**
     * Compile a fragment to string and serve visions from it
     */
    public fun serveVisionsFromFragment(
        route: String,
        fragment: HtmlVisionFragment,
    ): String = createHTML().apply {
        val visions = visionFragment(
            visionManager.context,
            embedData = true,
            fetchUpdatesUrl = "$serverUrl$route/ws",
            renderScript = true,
            fragment = fragment
        )
        serveVisions(route, visions)
    }.finalize()

    /**
     * Serve a page, potentially containing any number of visions at a given [route] with given [header].
     */
    public fun page(
        route: String = DEFAULT_PAGE,
        headers: Map<String, HtmlFragment>,
        visionFragment: HtmlVisionFragment,
    ) {
        val visions = HashMap<Name, Vision>()

        val cachedHtml: String? = if (cacheFragments) {
            //Create and cache page html and map of visions
            createHTML(true).html {
                visions.putAll(visionPage(route, headers, visionFragment))
            }
        } else {
            null
        }

        root.route(route) {
            serveVisions(this, visions)
            //filled pages
            get {
                if (cachedHtml == null) {
                    //re-create html and vision list on each call
                    call.respondHtml {
                        visions.clear()
                        visions.putAll(visionPage(route, headers, visionFragment))
                    }
                } else {
                    //Use cached html
                    call.respondText(cachedHtml, ContentType.Text.Html.withCharset(Charsets.UTF_8))
                }
            }
        }
    }

    public fun page(
        vararg headers: HtmlFragment,
        route: String = DEFAULT_PAGE,
        title: String = "VisionForge server page '$route'",
        visionFragment: HtmlVisionFragment,
    ) {
        page(route, mapOf("title" to VisionPage.title(title)) + headers.associateBy { it.hashCode().toString() }, visionFragment)
    }

    /**
     * Render given [VisionPage] at server
     */
    public fun page(route: String, page: VisionPage) {
        page(route = route, headers = page.pageHeaders, visionFragment = page.content)
    }

    public companion object {
        public const val DEFAULT_PORT: Int = 7777
        public const val DEFAULT_PAGE: String = "/"
        public val UPDATE_INTERVAL_KEY: Name = Name.parse("update.interval")
    }
}

/**
 * Attach VisionForge server application to given server
 */
public fun Application.visionServer(
    visionManager: VisionManager,
    webServerUrl: Url,
    path: String = DEFAULT_PAGE,
): VisionServer {
    install(WebSockets)
    install(CORS) {
        anyHost()
    }

//    if (pluginOrNull(CallLogging) == null) {
//        install(CallLogging)
//    }

    val serverRoute = install(Routing).createRouteFromPath(path)

    serverRoute {
        static {
            resources()
        }
    }

    return VisionServer(visionManager, URLBuilder(webServerUrl).apply { encodedPath = path }.build(), serverRoute)
}

/**
 * Start a stand-alone VisionForge server at given host/port
 */
public fun VisionManager.serve(
    host: String = "localhost",
    port: Int = VisionServer.DEFAULT_PORT,
    block: VisionServer.() -> Unit,
): ApplicationEngine = context.embeddedServer(CIO, port, host) {
    val url = URLBuilder(host = host, port = port).build()
    visionServer(this@serve, url).apply(block)
}.start()

/**
 * Connect to a given Ktor server using browser
 */
public fun ApplicationEngine.openInBrowser() {
    val connector = environment.connectors.first()
    val uri = URI("http", null, connector.host, connector.port, null, null, null)
    Desktop.getDesktop().browse(uri)
}

/**
 * Stop the server with default timeouts
 */
public fun ApplicationEngine.close(): Unit = stop(1000, 5000)