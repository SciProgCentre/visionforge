package space.kscience.visionforge.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
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
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.html.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionChange
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.flowChanges
import space.kscience.visionforge.html.*
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

public class VisionRouteConfiguration(
    public val visionManager: VisionManager,
    override val meta: ObservableMutableMeta = MutableMeta(),
) : Configurable, ContextAware {

    override val context: Context get() = visionManager.context

    /**
     * Update minimal interval between updates in milliseconds (if there are no updates, push will not happen
     */
    public var updateInterval: Long by meta.long(300, key = UPDATE_INTERVAL_KEY)

    public var dataMode: DataServeMode by meta.enum(DataServeMode.UPDATE)

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
public fun Route.serveVisionData(
    configuration: VisionRouteConfiguration,
    resolveVision: (Name) -> Vision?,
) {
    application.log.info("Serving visions at ${this@serveVisionData}")

    //Update websocket
    webSocket("ws") {
        val name: String = call.request.queryParameters.getOrFail("name")
        application.log.debug("Opened server socket for $name")
        val vision: Vision = resolveVision(Name.parse(name)) ?: error("Plot with id='$name' not registered")

        launch {
            incoming.consumeEach {
                val data = it.data.decodeToString()
                application.log.debug("Received update: \n$data")
                val change = configuration.visionManager.jsonFormat.decodeFromString(
                    VisionChange.serializer(), data
                )
                vision.update(change)
            }
        }

        try {
            withContext(configuration.context.coroutineContext) {
                vision.flowChanges(configuration.updateInterval.milliseconds).onEach { update ->
                    val json = configuration.visionManager.jsonFormat.encodeToString(
                        VisionChange.serializer(),
                        update
                    )
                    application.log.debug("Sending update: \n$json")
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

public fun Route.serveVisionData(
    configuration: VisionRouteConfiguration,
    cache: VisionCollector,
): Unit = serveVisionData(configuration) { cache[it]?.second }
//
///**
// * Compile a fragment to string and serve visions from it
// */
//public fun Route.serveVisionsFromFragment(
//    consumer: TagConsumer<*>,
//    sererPageUrl: Url,
//    visionManager: VisionManager,
//    fragment: HtmlVisionFragment,
//): Unit {
//    val visions = consumer.visionFragment(
//        visionManager.context,
//        embedData = true,
//        fetchUpdatesUrl = "$serverUrl$route/ws",
//        fragment = fragment
//    )
//
//    serveVisionData(visionManager, visions)
//}


/**
 * Serve a page, potentially containing any number of visions at a given [route] with given [header].
 */
public fun Route.visionPage(
    configuration: VisionRouteConfiguration,
    headers: Collection<HtmlFragment>,
    visionFragment: HtmlVisionFragment,
) {
    application.require(WebSockets)
    require(CORS) {
        anyHost()
    }

    val visionCache: VisionCollector = mutableMapOf()
    serveVisionData(configuration, visionCache)

    //filled pages
    get {
        //re-create html and vision list on each call
        call.respondHtml {
            val callbackUrl = call.url()
            head {
                meta {
                    charset = "utf-8"
                }
                headers.forEach { header ->
                    consumer.header()
                }
            }
            body {
                //Load the fragment and remember all loaded visions
                visionFragment(
                    context = configuration.context,
                    embedData = configuration.dataMode == DataServeMode.EMBED,
                    fetchDataUrl = if (configuration.dataMode != DataServeMode.EMBED) {
                        URLBuilder(callbackUrl).apply {
                            pathSegments = pathSegments + "data"
                        }.buildString()
                    } else null,
                    updatesUrl = if (configuration.dataMode == DataServeMode.UPDATE) {
                        URLBuilder(callbackUrl).apply {
                            protocol = URLProtocol.WS
                            pathSegments = pathSegments + "ws"
                        }.buildString()
                    } else null,
                    visionCache = visionCache,
                    fragment = visionFragment
                )
            }
        }

    }
}

public fun Route.visionPage(
    visionManager: VisionManager,
    vararg headers: HtmlFragment,
    configurationBuilder: VisionRouteConfiguration.() -> Unit = {},
    visionFragment: HtmlVisionFragment,
) {
    val configuration = VisionRouteConfiguration(visionManager).apply(configurationBuilder)
    visionPage(configuration, listOf(*headers), visionFragment)
}

/**
 * Render given [VisionPage] at server
 */
public fun Route.visionPage(page: VisionPage, configurationBuilder: VisionRouteConfiguration.() -> Unit = {}) {
    val configuration = VisionRouteConfiguration(page.visionManager).apply(configurationBuilder)
    visionPage(configuration, page.pageHeaders.values, visionFragment = page.content)
}

public fun <P : Pipeline<*, ApplicationCall>, B : Any, F : Any> P.require(
    plugin: Plugin<P, B, F>,
    configure: B.() -> Unit = {},
): F = pluginOrNull(plugin) ?: install(plugin, configure)


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