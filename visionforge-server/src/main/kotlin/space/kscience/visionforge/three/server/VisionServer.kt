package space.kscience.visionforge.three.server

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
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.fetch
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionChange
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.flowChanges
import space.kscience.visionforge.html.HtmlFragment
import space.kscience.visionforge.html.HtmlVisionFragment
import space.kscience.visionforge.html.VisionTagConsumer
import space.kscience.visionforge.html.fragment
import space.kscience.visionforge.three.server.VisionServer.Companion.DEFAULT_PAGE
import java.awt.Desktop
import java.net.URI
import kotlin.collections.set
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


/**
 * A ktor plugin container with given [routing]
 */
public class VisionServer internal constructor(
    private val visionManager: VisionManager,
    private val application: Application,
    private val rootRoute: String,
) : Configurable, CoroutineScope by application {
    override val meta: ObservableMutableMeta = MutableMeta()
    public var updateInterval: Long by meta.long(300, key = UPDATE_INTERVAL_KEY)
    public var cacheFragments: Boolean by meta.boolean(true)
    public var dataEmbed: Boolean by meta.boolean(true, Name.parse("data.embed"))
    public var dataFetch: Boolean by meta.boolean(false, Name.parse("data.fetch"))
    public var dataConnect: Boolean by meta.boolean(true, Name.parse("data.connect"))

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

        val consumer = object : VisionTagConsumer<Any?>(consumer, visionManager) {
            override fun DIV.renderVision(name: Name, vision: Vision, outputMeta: Meta) {
                visionMap[name] = vision
                // Toggle update mode
                if (dataConnect) {
                    attributes[OUTPUT_CONNECT_ATTRIBUTE] = "auto"
                }

                if (dataFetch) {
                    attributes[OUTPUT_FETCH_ATTRIBUTE] = "auto"
                }

                if (dataEmbed) {
                    script {
                        type = "text/json"
                        attributes["class"] = OUTPUT_DATA_CLASS
                        unsafe {
                            +"\n${visionManager.encodeToString(vision)}\n"
                        }
                    }
                }
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
            val vision: Vision = visions[Name.parse(name)] ?: error("Plot with id='$name' not registered")

            launch {
                incoming.consumeEach {
                    val change = visionManager.jsonFormat.decodeFromString(
                        VisionChange.serializer(), it.data.decodeToString()
                    )
                    vision.update(change)
                }
            }

            try {
                withContext(visionManager.context.coroutineContext) {
                    vision.flowChanges(visionManager, updateInterval.milliseconds).collect { update ->
                        val json = visionManager.jsonFormat.encodeToString(
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
        public val UPDATE_INTERVAL_KEY: Name = Name.parse("update.interval")
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

    val visionManager = context.fetch(VisionManager)

    routing {
        route(route) {
            static {
                resources()
            }
        }
    }

    return VisionServer(visionManager, this, route)
}

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