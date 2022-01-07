package space.kscience.visionforge.jupyter

import io.ktor.server.engine.ApplicationEngine
import kotlinx.html.FORM
import kotlinx.html.TagConsumer
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import kotlinx.html.style
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
import space.kscience.visionforge.three.server.VisionServer
import space.kscience.visionforge.three.server.serve
import space.kscience.visionforge.visionManager

/**
 * A handler class that includes a server and common utilities
 */
public class VisionForgeForNotebook(override val context: Context) : ContextAware {
    private var counter = 0

    private var engine: ApplicationEngine? = null
    private var server: VisionServer? = null

    public var isolateFragments: Boolean = false

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
        }?.stop(1000, 2000)
    }

    private fun produceHtmlString(
        fragment: HtmlVisionFragment,
    ): String = server?.serveVisionsFromFragment("content[${counter++}]", fragment)
        ?: createHTML().apply {
            visionFragment(context.visionManager, fragment = fragment)
        }.finalize()

    public fun produceHtml(isolated: Boolean? = null, fragment: HtmlVisionFragment): MimeTypedResult =
        HTML(produceHtmlString(fragment), isolated ?: isolateFragments)

    public fun fragment(body: HtmlVisionFragment): MimeTypedResult = produceHtml(fragment = body)
    public fun page(body: HtmlVisionFragment): MimeTypedResult = produceHtml(true, body)

    public fun form(builder: FORM.() -> Unit): HtmlFormFragment =
        HtmlFormFragment("form[${counter++}]", builder = builder)
}