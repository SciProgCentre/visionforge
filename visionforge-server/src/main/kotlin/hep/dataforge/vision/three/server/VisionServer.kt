package hep.dataforge.vision.three.server

import hep.dataforge.context.Context
import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionChange
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.flowChanges
import hep.dataforge.vision.html.HtmlFragment
import hep.dataforge.vision.html.HtmlVisionFragment
import hep.dataforge.vision.html.VisionTagConsumer
import hep.dataforge.vision.html.fragment
import hep.dataforge.vision.three.server.VisionServer.Companion.DEFAULT_PAGE
import io.ktor.application.*
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.http.withCharset
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.util.KtorExperimentalAPI
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.awt.Desktop
import java.net.URI
import kotlin.time.milliseconds

/**
 * A ktor plugin container with given [routing]
 */
public class VisionServer internal constructor(
    private val visionManager: VisionManager,
    private val application: Application,
    private val rootRoute: String,
) : Configurable, CoroutineScope by application {
    override val config: Config = Config()
    public var updateInterval: Long by config.long(300, key = UPDATE_INTERVAL_KEY)
    public var cacheFragments: Boolean by config.boolean(true)

    /**
     * a list of headers that should be applied to all pages
     */
    private val globalHeaders: ArrayList<HtmlFragment> = ArrayList()

    public fun header(block: TagConsumer<*>.() -> Unit) {
        globalHeaders.add(block)
    }

    private fun HTML.buildPage(
        visionFragment: HtmlVisionFragment,
        title: String,
        headers: List<HtmlFragment>,
    ): Map<Name, Vision> {
        val visionMap = HashMap<Name, Vision>()

        val consumer = object : VisionTagConsumer<Any?>(consumer) {
            override fun DIV.renderVision(name: Name, vision: Vision, outputMeta: Meta) {
                visionMap[name] = vision

                // Toggle updates
                attributes[OUTPUT_FETCH_VISION_ATTRIBUTE] = "true"
                attributes[OUTPUT_FETCH_UPDATE_ATTRIBUTE] = "true"
            }
        }

        head {
            meta {
                charset = "utf-8"
                (globalHeaders + headers).forEach {
                    fragment(it)
                }
            }
            title(title)
        }
        body {
            //Load the fragment and remember all loaded visions
            visionFragment(consumer)
        }

        return visionMap
    }

    /**
     * Server a map of visions without providing explicit html page for them
     */
    @OptIn(DFExperimental::class)
    public fun serveVisions(route: Route, visions: Map<Name, Vision>): Unit = route {
        application.log.info("Serving visions $visions at $route")

        //Update websocket
        webSocket("ws") {
            val name: String = call.request.queryParameters["name"]
                ?: error("Vision name is not defined in parameters")

            application.log.debug("Opened server socket for $name")
            val vision: Vision = visions[name.toName()] ?: error("Plot with id='$name' not registered")
            try {
                withContext(visionManager.context.coroutineContext) {
                    vision.flowChanges(visionManager, updateInterval.milliseconds).collect { update ->
                        val json = VisionManager.defaultJson.encodeToString(
                            VisionChange.serializer(),
                            update
                        )
                        outgoing.send(Frame.Text(json))
                    }
                }
            } catch (t: Throwable) {
                application.log.info("WebSocket update channel for $name is closed with exception: $t")
            }
        }
        //Plots in their json representation
        get("vision") {
            val name: String = call.request.queryParameters["name"]
                ?: error("Vision name is not defined in parameters")

            val vision: Vision? = visions[name.toName()]
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
     * Serv visions in a given [route] without providing a page template
     */
    public fun serveVisions(route: String, visions: Map<Name, Vision>): Unit {
        application.routing {
            route(rootRoute) {
                route(route) {
                    serveVisions(this, visions)
                }
            }
        }
    }

    /**
     * Serve a page, potentially containing any number of visions at a given [route] with given [headers].
     *
     */
    public fun page(
        route: String = DEFAULT_PAGE,
        title: String = "VisionForge server page '$route'",
        headers: List<HtmlFragment> = emptyList(),
        visionFragment: HtmlVisionFragment,
    ) {
        val visions = HashMap<Name, Vision>()

        val cachedHtml: String? = if (cacheFragments) {
            //Create and cache page html and map of visions
            createHTML(true).html {
                visions.putAll(buildPage(visionFragment, title, headers))
            }
        } else {
            null
        }

        application.routing {
            route(rootRoute) {
                route(route) {
                    serveVisions(this, visions)
                    //filled pages
                    get {
                        if (cachedHtml == null) {
                            //re-create html and vision list on each call
                            call.respondHtml {
                                visions.clear()
                                visions.putAll(buildPage(visionFragment, title, headers))
                            }
                        } else {
                            //Use cached html
                            call.respondText(cachedHtml, ContentType.Text.Html.withCharset(Charsets.UTF_8))
                        }
                    }
                }
            }
        }
    }

    public companion object {
        public const val DEFAULT_PAGE: String = "/"
        public val UPDATE_INTERVAL_KEY: Name = "update.interval".toName()
    }
}

/**
 * Use a script with given [src] as a global header for all pages.
 */
public inline fun VisionServer.useScript(src: String, crossinline block: SCRIPT.() -> Unit = {}) {
    header {
        script {
            type = "text/javascript"
            this.src = src
            block()
        }
    }
}

public inline fun VisionServer.useCss(href: String, crossinline block: LINK.() -> Unit = {}) {
    header {
        link {
            rel = "stylesheet"
            this.href = href
            block()
        }
    }
}

/**
 * Attach plotly application to given server
 */
public fun Application.visionServer(context: Context, route: String = DEFAULT_PAGE): VisionServer {
    if (featureOrNull(WebSockets) == null) {
        install(WebSockets)
    }

    if (featureOrNull(CORS) == null) {
        install(CORS) {
            anyHost()
        }
    }

    if (featureOrNull(CallLogging) == null) {
        install(CallLogging)
    }

    val visionManager = context.plugins.fetch(VisionManager)

    routing {
        route(route) {
            static {
                resources()
            }
        }
    }

    return VisionServer(visionManager, this, route)
}

@OptIn(KtorExperimentalAPI::class)
public fun VisionManager.serve(
    host: String = "localhost",
    port: Int = 7777,
    block: VisionServer.() -> Unit,
): ApplicationEngine = context.embeddedServer(CIO, port, host) {
    visionServer(context).apply(block)
}.start()

public fun ApplicationEngine.show() {
    val connector = environment.connectors.first()
    val uri = URI("http", null, connector.host, connector.port, null, null, null)
    Desktop.getDesktop().browse(uri)
}

public fun ApplicationEngine.close(): Unit = stop(1000, 5000)