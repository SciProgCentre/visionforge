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
import org.jetbrains.kotlinx.jupyter.api.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.context.info
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.html.HtmlVisionFragment
import space.kscience.visionforge.html.VisionDisplay
import space.kscience.visionforge.html.visionFragment
import space.kscience.visionforge.server.VisionRoute
import space.kscience.visionforge.server.serveVisionData
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.random.nextUInt


@Suppress("FunctionName")
internal inline fun HTML(isolated: Boolean = false, block: TagConsumer<*>.() -> Unit): MimeTypedResult =
    HTML(createHTML().apply(block).finalize(), isolated)

internal fun KotlinKernelHost.displayHtml(block: TagConsumer<*>.() -> Unit) {
    display(HTML(false, block), null)
}

public enum class VisionForgeCompatibility {
    JUPYTER,
    JUPYTER_LAB,
    DATALORE,
    IDEA
}

/**
 * A handler class that includes a server and common utilities
 */
@Suppress("ExtractKtorModule")
public class VisionForge(
    public val visionManager: VisionManager,
    public val notebook: Notebook,
    meta: Meta = Meta.EMPTY,
) : ContextAware, CoroutineScope {

    override val context: Context get() = visionManager.context

    public val configuration: ObservableMutableMeta = meta.toMutableMeta().asObservable()

    private var counter = 0

    private var engine: ApplicationEngine? = null

    override val coroutineContext: CoroutineContext get() = context.coroutineContext


    public fun isServerRunning(): Boolean = engine != null

    public fun getProperty(name: String): TypedMeta<*>? = configuration[name] ?: context.properties[name]

    internal fun startServer(
        kernel: KotlinKernelHost,
        host: String = getProperty("visionforge.host").string ?: "localhost",
        port: Int = getProperty("visionforge.port").int ?: VisionRoute.DEFAULT_PORT,
    ) {
        engine?.let {
            kernel.displayHtml {
                p {
                    style = "color: red;"
                    +"Stopping current VisionForge server"
                }
            }
            it.stop(1000, 2000)
        }

        //val connector: EngineConnectorConfig = EngineConnectorConfig(host, port)


        engine = context.embeddedServer(CIO, port, host) {
            install(WebSockets)
        }.start(false)

        kernel.displayHtml {
            p {
                style = "color: blue;"
                +"Starting VisionForge server on port $port"
            }
        }
    }

    internal fun stopServer(kernel: KotlinKernelHost) {
        engine?.apply {
            logger.info { "Stopping VisionForge server" }
            stop(1000, 2000)
            engine = null
        }

        kernel.displayHtml {
            p {
                style = "color: red;"
                +"VisionForge server stopped"
            }
        }
    }

    internal fun TagConsumer<*>.renderScriptForId(id: String) {
        script {
            type = "text/javascript"
            //language=JavaScript
            unsafe { +"parent.VisionForge.renderAllVisionsById(document, \"$id\");" }
        }
    }


    public fun produceHtml(
        isolated: Boolean? = null,
        fragment: HtmlVisionFragment,
    ): MimeTypedResult {
        val iframeIsolation = isolated ?: when (notebook.jupyterClientType) {
            JupyterClientType.DATALORE, JupyterClientType.JUPYTER_NOTEBOOK -> true
            else -> false
        }
        return HTML(
            iframeIsolation
        ) {
            val id = "fragment[${fragment.hashCode()}/${Random.nextUInt()}]"
            div {
                this.id = id
                val engine = engine
                if (engine != null) {
                    //if server exist, serve dynamically
                    //server.serveVisionsFromFragment(consumer, "content-${counter++}", fragment)
                    val cellRoute = "content-${counter++}"

                    val cache: MutableMap<Name, VisionDisplay> = mutableMapOf()

                    val url = engine.environment.connectors.first().let {
                        url {
                            protocol = URLProtocol.WS
                            host = it.host
                            port = it.port
                            pathSegments = listOf(cellRoute, "ws")
                        }
                    }

                    engine.application.serveVisionData(VisionRoute(cellRoute, visionManager), cache)

                    visionFragment(
                        visionManager,
                        embedData = true,
                        updatesUrl = url,
                        displayCache = cache,
                        fragment = fragment
                    )
                } else {
                    //if not, use static rendering
                    visionFragment(visionManager, fragment = fragment)
                }
            }
            renderScriptForId(id)
        }
    }

    public fun form(builder: FORM.() -> Unit): HtmlFormFragment =
        HtmlFormFragment("form[${counter++}]", builder = builder)
}

