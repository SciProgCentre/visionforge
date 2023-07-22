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
import org.jetbrains.kotlinx.jupyter.api.KotlinKernelHost
import org.jetbrains.kotlinx.jupyter.api.MimeTypedResult
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.context.info
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.html.*
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
    meta: Meta = Meta.EMPTY,
) : ContextAware, CoroutineScope {

    override val context: Context get() = visionManager.context

    public val configuration: ObservableMutableMeta = meta.toMutableMeta()

    private var counter = 0

    private var engine: ApplicationEngine? = null

    public var notebookMode: VisionForgeCompatibility = VisionForgeCompatibility.IDEA

    override val coroutineContext: CoroutineContext get() = context.coroutineContext


    public fun isServerRunning(): Boolean = engine != null

    public fun getProperty(name: String): TypedMeta<*>? = configuration[name] ?: context.properties[name]

    internal fun startServer(
        kernel: KotlinKernelHost,
        host: String = getProperty("visionforge.host").string ?: "localhost",
        port: Int = getProperty("visionforge.port").int ?: VisionRoute.DEFAULT_PORT,
    ) {
        if (engine != null) {
            kernel.displayHtml {
                p {
                    style = "color: red;"
                    +"Stopping current VisionForge server"
                }
            }

        }

        //val connector: EngineConnectorConfig = EngineConnectorConfig(host, port)

        engine?.stop(1000, 2000)
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

    internal fun TagConsumer<*>.renderScriptForId(id: String, iframeIsolation: Boolean = false) {
        script {
            type = "text/javascript"
            if (iframeIsolation) {
                //language=JavaScript
                unsafe { +"parent.VisionForge.renderAllVisionsById(document, \"$id\");" }
            } else {
                //language=JavaScript
                unsafe { +"VisionForge.renderAllVisionsById(document, \"$id\");" }
            }
        }
    }


    public fun produceHtml(
        isolated: Boolean? = null,
        fragment: HtmlVisionFragment,
    ): MimeTypedResult {
        val iframeIsolation = isolated
            ?: (notebookMode == VisionForgeCompatibility.JUPYTER || notebookMode == VisionForgeCompatibility.DATALORE)
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
            renderScriptForId(id, iframeIsolation = iframeIsolation)
        }
    }

    public fun form(builder: FORM.() -> Unit): HtmlFormFragment =
        HtmlFormFragment("form[${counter++}]", builder = builder)
}

