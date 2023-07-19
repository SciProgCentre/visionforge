package space.kscience.visionforge.jupyter

import io.ktor.http.URLProtocol
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.util.url
import io.ktor.server.websocket.WebSockets
import kotlinx.coroutines.CoroutineScope
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.MimeTypedResult
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.context.info
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.html.HtmlVisionFragment
import space.kscience.visionforge.html.visionFragment
import space.kscience.visionforge.server.VisionRoute
import space.kscience.visionforge.server.serveVisionData
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.random.nextUInt

internal fun TagConsumer<*>.renderScriptForId(id: String) {
    script {
        type = "text/javascript"
        unsafe { +"VisionForge.renderAllVisionsById(\"$id\");" }
    }
}

/**
 * A handler class that includes a server and common utilities
 */
public class VisionForge(
    public val visionManager: VisionManager,
    meta: Meta = Meta.EMPTY,
) : ContextAware, CoroutineScope {

    override val context: Context get() = visionManager.context

    public val configuration: ObservableMutableMeta = meta.toMutableMeta()

    private var counter = 0

    private var engine: ApplicationEngine? = null

    public var isolateFragments: Boolean = false

    override val coroutineContext: CoroutineContext get() = context.coroutineContext

    public fun legacyMode() {
        isolateFragments = true
    }

    public fun isServerRunning(): Boolean = engine != null

    public fun html(block: TagConsumer<*>.() -> Unit): MimeTypedResult = HTML(createHTML().apply(block).finalize())

    public fun getProperty(name: String): TypedMeta<*>? = configuration[name] ?: context.properties[name]

    public fun startServer(
        host: String = getProperty("visionforge.host").string ?: "localhost",
        port: Int = getProperty("visionforge.port").int ?: VisionRoute.DEFAULT_PORT,
    ): MimeTypedResult = html {
        if (engine != null) {
            p {
                style = "color: red;"
                +"Stopping current VisionForge server"
            }
        }

        //val connector: EngineConnectorConfig = EngineConnectorConfig(host, port)

        engine?.stop(1000, 2000)
        engine = context.embeddedServer(CIO, port, host) {
            install(WebSockets)
        }.start(false)

        p {
            style = "color: blue;"
            +"Starting VisionForge server on http://$host:$port"
        }
    }

    public fun stopServer() {
        engine?.apply {
            logger.info { "Stopping VisionForge server" }
            stop(1000, 2000)
            engine = null
        }
    }

    private fun produceHtmlString(
        fragment: HtmlVisionFragment,
    ): String = createHTML().apply {
        val id = "fragment[${fragment.hashCode()}/${Random.nextUInt()}]"
        div {
            this.id = id
            val engine = engine
            if (engine != null) {
                //if server exist, serve dynamically
                //server.serveVisionsFromFragment(consumer, "content-${counter++}", fragment)
                val cellRoute = "content-${counter++}"

                val collector: MutableMap<Name, Vision> = mutableMapOf()

                val url = engine.environment.connectors.first().let {
                    url {
                        protocol = URLProtocol.WS
                        host = it.host
                        port = it.port
                        pathSegments = listOf(cellRoute, "ws")
                    }
                }

                engine.application.serveVisionData(VisionRoute(cellRoute, visionManager), collector)

                visionFragment(
                    visionManager,
                    embedData = true,
                    updatesUrl = url,
                    onVisionRendered = { name, vision -> collector[name] = vision },
                    fragment = fragment
                )
            } else {
                //if not, use static rendering
                visionFragment(visionManager, fragment = fragment)
            }
        }
        renderScriptForId(id)
    }.finalize()

    public fun produceHtml(isolated: Boolean? = null, fragment: HtmlVisionFragment): MimeTypedResult =
        HTML(produceHtmlString(fragment), isolated ?: isolateFragments)

    public fun fragment(body: HtmlVisionFragment): MimeTypedResult = produceHtml(fragment = body)
    public fun page(body: HtmlVisionFragment): MimeTypedResult = produceHtml(true, body)

    public fun form(builder: FORM.() -> Unit): HtmlFormFragment =
        HtmlFormFragment("form[${counter++}]", builder = builder)
}
