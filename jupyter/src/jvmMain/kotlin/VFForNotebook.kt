package space.kscience.visionforge.jupyter

import io.ktor.server.engine.ApplicationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.MimeTypedResult
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.context.info
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.string
import space.kscience.visionforge.html.HtmlFormFragment
import space.kscience.visionforge.html.HtmlVisionFragment
import space.kscience.visionforge.html.visionFragment
import space.kscience.visionforge.server.VisionServer
import space.kscience.visionforge.server.serve
import space.kscience.visionforge.visionManager
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
public class VFForNotebook(override val context: Context) : ContextAware, CoroutineScope {
    private var counter = 0

    private var engine: ApplicationEngine? = null
    private var server: VisionServer? = null

    public var isolateFragments: Boolean = false

    override val coroutineContext: CoroutineContext get() = context.coroutineContext

    public fun legacyMode() {
        isolateFragments = true
    }

    public fun isServerRunning(): Boolean = server != null

    public fun html(block: TagConsumer<*>.() -> Unit): MimeTypedResult = HTML(createHTML().apply(block).finalize())

    public fun startServer(
        host: String = context.properties["visionforge.host"].string ?: "localhost",
        port: Int = context.properties["visionforge.port"].int ?: VisionServer.DEFAULT_PORT,
        configuration: VisionServer.() -> Unit = {},
    ): MimeTypedResult = html {
        if (server != null) {
            p {
                style = "color: red;"
                +"Stopping current VisionForge server"
            }
        }

        engine?.stop(1000, 2000)
        engine = context.visionManager.serve(host, port) {
            configuration()
            server = this
        }.start()

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
            server = null
        }
    }

    private fun produceHtmlString(
        fragment: HtmlVisionFragment,
    ): String = createHTML().apply {
        val server = server
        val id = "fragment[${fragment.hashCode()}/${Random.nextUInt()}]"
        div {
            this.id = id
            if (server != null) {
                //if server exist, serve dynamically
                server.serveVisionsFromFragment(consumer, "content-${counter++}", fragment)
            } else {
                //if not, use static rendering
                visionFragment(context, fragment = fragment)
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