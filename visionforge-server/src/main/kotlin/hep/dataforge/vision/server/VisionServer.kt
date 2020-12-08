package hep.dataforge.vision.server

import hep.dataforge.context.Context
import hep.dataforge.meta.Config
import hep.dataforge.meta.Configurable
import hep.dataforge.meta.boolean
import hep.dataforge.meta.long
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionChange
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.flowChanges
import hep.dataforge.vision.html.*
import hep.dataforge.vision.server.VisionServer.Companion.DEFAULT_PAGE
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
import io.ktor.routing.application
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.util.error
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.flow.collect
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
) : Configurable {
    override val config: Config = Config()
    public var updateInterval: Long by config.long(300, key = UPDATE_INTERVAL_KEY)
    public var cacheFragments: Boolean by config.boolean(true)

    /**
     * a list of headers that should be applied to all pages
     */
    private val globalHeaders: ArrayList<HtmlFragment> = ArrayList()

    public fun header(block: TagConsumer<*>.() -> Unit) {
        globalHeaders.add(HtmlFragment(block))
    }

    private fun HTML.buildPage(
        visionFragment: HtmlVisionFragment<Vision>,
        title: String,
        headers: List<HtmlFragment>,
    ): Map<Name, Vision> {
        lateinit var visionMap: Map<Name, Vision>

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
//            attributes[OUTPUT_ENDPOINT_ATTRIBUTE] = if (rootRoute.endsWith("/")) {
//                rootRoute
//            } else {
//                "$rootRoute/"
//            }
            //Load the fragment and remember all loaded visions
            visionMap = visionFragment(visionFragment)
//            //The script runs when all headers already with required libraries are already loaded
//            script {
//                type = "text/javascript"
//
//                val normalizedRoute =
//                unsafe {
//                    //language=JavaScript
//                    +"fetchAndRenderAllVisions()"
//                }
//            }
        }

        return visionMap
    }

    public fun page(
        visionFragment: HtmlVisionFragment<Vision>,
        route: String = DEFAULT_PAGE,
        title: String = "VisionForge server page '$route'",
        headers: List<HtmlFragment> = emptyList(),
    ) {
        val visions = HashMap<Name, Vision>()

        val cachedHtml: String? = if (cacheFragments) {
            createHTML(true).html {
                visions.putAll(buildPage(visionFragment, title, headers))
            }
        } else {
            null
        }

        application.routing {
            route(rootRoute) {
                route(route) {
                    //Update websocket
                    webSocket("ws") {
                        val name: String = call.request.queryParameters["name"]
                            ?: error("Vision name is not defined in parameters")

                        application.log.debug("Opened server socket for $name")
                        val vision: Vision = visions[name.toName()] ?: error("Plot with id='$name' not registered")
                        try {
                            vision.flowChanges(visionManager, updateInterval.milliseconds).collect { update ->
                                val json = VisionManager.defaultJson.encodeToString(VisionChange.serializer(), update)
                                outgoing.send(Frame.Text(json))
                            }
                        } catch (ex: Throwable) {
                            application.log.error("Closed server socket for $name with exception $ex")
                            application.log.error(ex)
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
                    //filled pages
                    get {
                        if (cachedHtml == null) {
                            call.respondHtml {
                                visions.putAll(buildPage(visionFragment, title, headers))
                            }
                        } else {
                            call.respondText(cachedHtml, ContentType.Text.Html.withCharset(Charsets.UTF_8))
                        }
                    }
                }
            }
        }
    }

    public fun page(
        route: String = DEFAULT_PAGE,
        title: String = "VisionForge server page '$route'",
        headers: List<HtmlFragment> = emptyList(),
        content: HtmlOutputScope<*, Vision>.() -> Unit,
    ) {
        page(buildVisionFragment(content), route, title, headers)
    }


    public companion object {
        public const val DEFAULT_PAGE: String = "/"
        public val UPDATE_INTERVAL_KEY: Name = "update.interval".toName()
    }
}


/**
 * Attach plotly application to given server
 */
public fun Application.visionModule(context: Context, route: String = DEFAULT_PAGE): VisionServer {
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

public fun ApplicationEngine.show() {
    val connector = environment.connectors.first()
    val uri = URI("http", null, connector.host, connector.port, null, null, null)
    Desktop.getDesktop().browse(uri)
}

public fun ApplicationEngine.close(): Unit = stop(1000, 5000)